package com.actionworks.flashsale.app.service.placeorder.normal.cache;

import com.actionworks.flashsale.app.service.stock.ItemStockCacheService;
import com.actionworks.flashsale.app.service.stock.model.ItemStockCache;
import com.actionworks.flashsale.app.util.MultiPlaceOrderTypesCondition;
import com.actionworks.flashsale.cache.DistributedCacheService;
import com.actionworks.flashsale.cache.redis.RedisCacheService;
import com.actionworks.flashsale.domain.model.StockDeduction;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.actionworks.flashsale.util.StringUtil.link;

@Service
@Conditional(MultiPlaceOrderTypesCondition.class)
public class NormalStockCacheService implements ItemStockCacheService {
    private static final String ITEM_STOCK_ALIGN_LOCK_KEY = "ITEM_STOCK_ALIGN_LOCK_KEY";
    private static final Logger logger = LoggerFactory.getLogger(NormalStockCacheService.class);
    private static final int IN_STOCK_ALIGNING = -9;
    private static final String INIT_OR_ALIGN_ITEM_STOCK_LUA;
    private static final String INCREASE_ITEM_STOCK_LUA;
    private static final String DECREASE_ITEM_STOCK_LUA;
    private final static Cache<Long, ItemStockCache> itemStockLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static final String ITEM_STOCKS_CACHE_KEY = "ITEM_STOCKS_CACHE_KEY";

    static {
        INIT_OR_ALIGN_ITEM_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -997;" +
                "end;" +
                "redis.call('set', KEYS[2] , 1);" +
                "local stockNumber = tonumber(ARGV[1]);" +
                "redis.call('set', KEYS[1] , stockNumber);" +
                "redis.call('del', KEYS[2]);" +
                "return 1";

        INCREASE_ITEM_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -9;" +
                "end;" +
                "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    local stock = tonumber(redis.call('get', KEYS[1]));" +
                "    local num = tonumber(ARGV[1]);" +
                "    redis.call('incrby', KEYS[1] , num);" +
                "    return 1;" +
                "end;" +
                "return -1;";


        DECREASE_ITEM_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -9;" +
                "end;" +
                "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    local stock = tonumber(redis.call('get', KEYS[1]));" +
                "    local num = tonumber(ARGV[1]);" +
                "    if (stock < num) then" +
                "        return -3" +
                "    end;" +
                "    if (stock >= num) then" +
                "        redis.call('incrby', KEYS[1], 0 - num);" +
                "        return 1" +
                "    end;" +
                "    return -2;" +
                "end;" +
                "return -1;";
    }

    @Resource
    private RedisCacheService redisCacheService;
    @Resource
    private FlashItemDomainService flashItemDomainService;
    @Resource
    private DistributedCacheService distributedCacheService;

    @Override
    public boolean alignItemStocks(Long itemId) {
        if (itemId == null) {
            logger.info("alignItemStocks|参数为空");
            return false;
        }
        try {
            FlashItem flashItem = flashItemDomainService.getFlashItem(itemId);
            if (flashItem == null) {
                logger.info("alignItemStocks|秒杀品不存在|{}", itemId);
                return false;
            }
            if (flashItem.getInitialStock() == null) {
                logger.info("alignItemStocks|秒杀品未设置库存|{}", itemId);
                return false;
            }
            String key1ItemStocksCacheKey = getItemStocksCacheKey(itemId);
            String key2ItemStocksAlignKey = getItemStocksCacheAlignKey(itemId);
            List<String> keys = Lists.newArrayList(key1ItemStocksCacheKey, key2ItemStocksAlignKey);

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INIT_OR_ALIGN_ITEM_STOCK_LUA, Long.class);
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys, flashItem.getAvailableStock());
            if (result == null) {
                logger.info("alignItemStocks|秒杀品库存校准失败|{},{},{}", itemId, key1ItemStocksCacheKey, flashItem.getInitialStock());
                return false;
            }
            if (result == -997) {
                logger.info("alignItemStocks|已在校准中，本次校准取消|{},{},{},{}", result, itemId, key1ItemStocksCacheKey, flashItem.getInitialStock());
                return true;
            }
            if (result == 1) {
                logger.info("alignItemStocks|秒杀品库存校准完成|{},{},{},{}", result, itemId, key1ItemStocksCacheKey, flashItem.getInitialStock());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("alignItemStocks|秒杀品库存校准错误|{}", itemId, e);
            return false;
        }
    }

    @Override
    public boolean decreaseItemStock(StockDeduction stockDeduction) {
        logger.info("decreaseItemStock|申请库存预扣减|{}", JSON.toJSONString(stockDeduction));
        if (stockDeduction == null || !stockDeduction.validate()) {
            return false;
        }
        try {
            String key1ItemStocksCacheKey = getItemStocksCacheKey(stockDeduction.getItemId());
            String key2ItemStocksCacheAlignKey = getItemStocksCacheAlignKey(stockDeduction.getItemId());
            List<String> keys = Lists.newArrayList(key1ItemStocksCacheKey, key2ItemStocksCacheAlignKey);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(DECREASE_ITEM_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                if (result == null) {
                    logger.info("decreaseItemStock|库存扣减失败|{}", key1ItemStocksCacheKey);
                    return false;
                }
                if (result == IN_STOCK_ALIGNING) {
                    logger.info("decreaseItemStock|库存校准中|{}", key1ItemStocksCacheKey);
                    Thread.sleep(20);
                }
                if (result == -1 || result == -2) {
                    logger.info("decreaseItemStock|库存扣减失败|{}", key1ItemStocksCacheKey);
                    return false;
                }
                if (result == -3) {
                    logger.info("decreaseItemStock|库存扣减失败|{}", key1ItemStocksCacheKey);
                    return false;
                }
                if (result == 1) {
                    logger.info("decreaseItemStock|库存扣减成功|{}", key1ItemStocksCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("decreaseItemStock|库存扣减失败", e);
            return false;
        }
        return false;
    }

    @Override
    public boolean increaseItemStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || !stockDeduction.validate()) {
            return false;
        }
        try {
            String key1ItemStocksCacheKey = getItemStocksCacheKey(stockDeduction.getItemId());
            String key2ItemStocksCacheAlignKey = getItemStocksCacheAlignKey(stockDeduction.getItemId());
            List<String> keys = Lists.newArrayList(key1ItemStocksCacheKey, key2ItemStocksCacheAlignKey);

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INCREASE_ITEM_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                if (result == null) {
                    logger.info("increaseItemStock|库存增加失败|{}", key1ItemStocksCacheKey);
                    return false;
                }
                if (result == IN_STOCK_ALIGNING) {
                    logger.info("increaseItemStock|库存校准中|{}", key1ItemStocksCacheKey);
                    Thread.sleep(20);
                }
                if (result == -1) {
                    logger.info("increaseItemStock|库存增加失败|{}", key1ItemStocksCacheKey);
                    return false;
                }
                if (result == 1) {
                    logger.info("increaseItemStock|库存增加成功|{}", key1ItemStocksCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("increaseItemStock|库存增加失败", e);
            return false;
        }
        return false;
    }

    @Override
    public ItemStockCache getAvailableItemStock(Long userId, Long itemId) {
        ItemStockCache itemStockCache = itemStockLocalCache.getIfPresent(itemId);
        if (itemStockCache != null) {
            return itemStockCache;
        }
        Integer availableStock = distributedCacheService.getObject(getItemStocksCacheKey(itemId), Integer.class);
        if (availableStock == null) {
            return null;
        }
        itemStockCache = new ItemStockCache().with(availableStock);
        itemStockLocalCache.put(itemId, itemStockCache);
        return itemStockCache;
    }

    public static String getItemStocksCacheAlignKey(Long itemId) {
        return link(ITEM_STOCK_ALIGN_LOCK_KEY, itemId);
    }

    public static String getItemStocksCacheKey(Long itemId) {
        return link(ITEM_STOCKS_CACHE_KEY, itemId);
    }
}
