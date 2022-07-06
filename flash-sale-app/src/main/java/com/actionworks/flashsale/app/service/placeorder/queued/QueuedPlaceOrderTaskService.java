package com.actionworks.flashsale.app.service.placeorder.queued;

import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.actionworks.flashsale.app.model.enums.OrderTaskStatus;
import com.actionworks.flashsale.app.model.result.OrderTaskSubmitResult;
import com.actionworks.flashsale.app.mq.OrderTaskPostService;
import com.actionworks.flashsale.app.service.stock.ItemStockCacheService;
import com.actionworks.flashsale.app.service.stock.model.ItemStockCache;
import com.actionworks.flashsale.cache.redis.RedisCacheService;
import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.actionworks.flashsale.app.exception.AppErrorCode.INVALID_PARAMS;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_TASK_SUBMIT_FAILED;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_TOKENS_NOT_AVAILABLE;
import static com.actionworks.flashsale.app.exception.AppErrorCode.REDUNDANT_SUBMIT;
import static com.actionworks.flashsale.app.model.constants.CacheConstants.HOURS_24;

@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class QueuedPlaceOrderTaskService implements PlaceOrderTaskService {
    private static final String LOCK_REFRESH_LATEST_AVAILABLE_TOKENS_KEY = "LOCK_REFRESH_LATEST_AVAILABLE_TOKENS_KEY_";
    private static final Logger logger = LoggerFactory.getLogger(QueuedPlaceOrderTaskService.class);
    private final static Cache<Long, Integer> availableOrderTokensLocalCache = CacheBuilder.newBuilder().initialCapacity(20).concurrencyLevel(5).expireAfterWrite(20, TimeUnit.MILLISECONDS).build();
    private static final String TAKE_ORDER_TOKEN_LUA;
    private static final String RECOVER_ORDER_TOKEN_LUA;
    private static final String PLACE_ORDER_TASK_ID_KEY = "PLACE_ORDER_TASK_ID_KEY_";
    private static final String PLACE_ORDER_TASK_AVAILABLE_TOKENS_KEY = "PLACE_ORDER_TASK_AVAILABLE_TOKENS_KEY_";

    static {
        TAKE_ORDER_TOKEN_LUA = "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    local availableTokensCount = tonumber(redis.call('get', KEYS[1]));" +
                "    if (availableTokensCount == 0) then" +
                "        return -1;" +
                "    end;" +
                "    if (availableTokensCount > 0) then" +
                "        redis.call('incrby', KEYS[1], -1);" +
                "        return 1;" +
                "    end;" +
                "end;" +
                "return -100;";
        RECOVER_ORDER_TOKEN_LUA = "if (redis.call('exists', KEYS[1]) == 1) then" +
                "   redis.call('incrby', KEYS[1], 1);" +
                "   return 1;" +
                "end;" +
                "return -100;";
    }

    @Resource
    private RedisCacheService redisCacheService;
    @Resource
    private ItemStockCacheService itemStockCacheService;
    @Resource
    private OrderTaskPostService orderTaskPostService;
    @Resource
    private DistributedLockFactoryService lockFactoryService;

    @Override
    public OrderTaskSubmitResult submit(PlaceOrderTask placeOrderTask) {
        logger.info("submitOrderTask|提交下单任务|{}", JSON.toJSONString(placeOrderTask));
        if (placeOrderTask == null) {
            return OrderTaskSubmitResult.failed(INVALID_PARAMS);
        }
        String taskKey = getOrderTaskKey(placeOrderTask.getPlaceOrderTaskId());
        Integer taskIdSubmittedResult = redisCacheService.getObject(taskKey, Integer.class);
        if (taskIdSubmittedResult != null) {
            return OrderTaskSubmitResult.failed(REDUNDANT_SUBMIT);
        }
        Integer availableOrderTokens = getAvailableOrderTokens(placeOrderTask.getItemId());
        if (availableOrderTokens == null || availableOrderTokens == 0) {
            return OrderTaskSubmitResult.failed(ORDER_TOKENS_NOT_AVAILABLE);
        }

        if (!takeOrRecoverToken(placeOrderTask, TAKE_ORDER_TOKEN_LUA)) {
            logger.info("submitOrderTask|库存扣减失败|{},{}", placeOrderTask.getUserId(), placeOrderTask.getPlaceOrderTaskId());
            return OrderTaskSubmitResult.failed(ORDER_TOKENS_NOT_AVAILABLE);
        }
        boolean postSuccess = orderTaskPostService.post(placeOrderTask);
        if (!postSuccess) {
            takeOrRecoverToken(placeOrderTask, RECOVER_ORDER_TOKEN_LUA);
            logger.info("submitOrderTask|下单任务提交失败|{},{}", placeOrderTask.getUserId(), placeOrderTask.getPlaceOrderTaskId());
            return OrderTaskSubmitResult.failed(ORDER_TASK_SUBMIT_FAILED);
        }
        redisCacheService.put(taskKey, 0, HOURS_24);
        logger.info("submitOrderTask|下单任务提交成功|{},{}", placeOrderTask.getUserId(), placeOrderTask.getPlaceOrderTaskId());
        return OrderTaskSubmitResult.ok();
    }

    private String getOrderTaskKey(String placeOrderTaskId) {
        return PLACE_ORDER_TASK_ID_KEY + placeOrderTaskId;
    }

    public void updateTaskHandleResult(String placeOrderTaskId, boolean result) {
        if (StringUtils.isEmpty(placeOrderTaskId)) {
            return;
        }
        String taskKey = getOrderTaskKey(placeOrderTaskId);
        Integer taskStatus = redisCacheService.getObject(taskKey, Integer.class);
        if (taskStatus == null || taskStatus != 0) {
            return;
        }
        redisCacheService.put(taskKey, result ? 1 : -1);
    }

    @Override
    public OrderTaskStatus getTaskStatus(String placeOrderTaskId) {
        String taskKey = getOrderTaskKey(placeOrderTaskId);
        Integer taskStatus = redisCacheService.getObject(taskKey, Integer.class);
        return OrderTaskStatus.findBy(taskStatus);
    }

    private boolean takeOrRecoverToken(PlaceOrderTask placeOrderTask, String script) {
        List<String> keys = new ArrayList<>();
        keys.add(getItemAvailableTokensKey(placeOrderTask.getItemId()));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        for (int i = 0; i < 3; i++) {
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys);
            if (result == null) {
                return false;
            }
            if (result == -100) {
                refreshLatestAvailableTokens(placeOrderTask.getItemId());
                continue;
            }
            return result == 1L;
        }
        return false;
    }

    private Integer getAvailableOrderTokens(Long itemId) {
        Integer availableOrderTokens = availableOrderTokensLocalCache.getIfPresent(itemId);
        if (availableOrderTokens != null) {
            return availableOrderTokens;
        }
        return refreshLocalAvailableTokens(itemId);
    }

    private synchronized Integer refreshLocalAvailableTokens(Long itemId) {
        Integer availableOrderTokens = availableOrderTokensLocalCache.getIfPresent(itemId);
        if (availableOrderTokens != null) {
            return availableOrderTokens;
        }
        String availableTokensKey = getItemAvailableTokensKey(itemId);
        Integer latestAvailableOrderTokens = redisCacheService.getObject(availableTokensKey, Integer.class);
        if (latestAvailableOrderTokens != null) {
            availableOrderTokensLocalCache.put(itemId, latestAvailableOrderTokens);
            return latestAvailableOrderTokens;
        }
        return refreshLatestAvailableTokens(itemId);
    }

    private Integer refreshLatestAvailableTokens(Long itemId) {
        DistributedLock refreshTokenLock = lockFactoryService.getDistributedLock(getRefreshTokensLockKey(itemId));
        try {
            boolean isLockSuccess = refreshTokenLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLockSuccess) {
                return null;
            }
            ItemStockCache itemStockCache = itemStockCacheService.getAvailableItemStock(null, itemId);
            if (itemStockCache != null && itemStockCache.isSuccess() && itemStockCache.getAvailableStock() != null) {
                Integer latestAvailableOrderTokens = (int) Math.ceil(itemStockCache.getAvailableStock() * 1.5);
                redisCacheService.put(getItemAvailableTokensKey(itemId), latestAvailableOrderTokens, HOURS_24);
                availableOrderTokensLocalCache.put(itemId, latestAvailableOrderTokens);
                return latestAvailableOrderTokens;
            }
        } catch (Exception e) {
            logger.error("refreshAvailableTokens|刷新tokens失败|{}", itemId, e);
        } finally {
            refreshTokenLock.unlock();
        }
        return null;
    }

    private String getRefreshTokensLockKey(Long itemId) {
        return LOCK_REFRESH_LATEST_AVAILABLE_TOKENS_KEY + itemId;
    }

    private String getItemAvailableTokensKey(Long itemId) {
        return PLACE_ORDER_TASK_AVAILABLE_TOKENS_KEY + itemId;
    }
}
