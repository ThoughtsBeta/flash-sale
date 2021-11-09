package com.actionworks.flashsale.app.cache;

import com.actionworks.flashsale.app.cache.model.ItemStockCache;
import com.actionworks.flashsale.cache.DistributedCacheService;
import com.actionworks.flashsale.cache.redis.RedisCacheService;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DefaultItemStockCacheService implements ItemStockCacheService {
    private static final int IN_STOCK_ALIGNING = -9;
    private static final String INIT_ITEM_STOCK_LUA;
    private static final String INCREASE_ITEM_STOCK_LUA;
    private static final String DECREASE_ITEM_STOCK_LUA;
    private static final String ALIGN_ITEM_STOCK_LUA;
    private static final Logger logger = LoggerFactory.getLogger(DefaultItemStockCacheService.class);
    private final static Cache<Long, ItemStockCache> itemStockLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static final String ITEM_STOCK_KEY = "ITEM_STOCK_KEY_";
    private static final String ITEM_STOCK_ALIGN_LOCK_KEY = "ITEM_STOCK_ALIGN_LOCK_KEY_";

    static {

        INIT_ITEM_STOCK_LUA = "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    return -1;" +
                "end;" +
                "local stockNumber = tonumber(ARGV[1]);" +
                "redis.call('set', KEYS[1] , stockNumber);" +
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

        ALIGN_ITEM_STOCK_LUA = "redis.call('set', KEYS[1] , 1);" +
                "local stockNumber = tonumber(ARGV[1]);" +
                "redis.call('set', KEYS[2] , stockNumber);" +
                "redis.call('del', KEYS[1]);" +
                "return 1";
    }

    @Resource
    private RedisCacheService redisCacheService;
    @Resource
    private FlashItemDomainService flashItemDomainService;
    @Resource
    private DistributedLockFactoryService distributedLockFactoryService;
    @Resource
    private DistributedCacheService distributedCacheService;

    @Override
    public boolean initItemStock(Long itemId) {
        if (itemId == null) {
            logger.info("initItemStock|参数为空");
            return false;
        }
        FlashItem flashItem = flashItemDomainService.getFlashItem(itemId);
        if (flashItem == null) {
            logger.info("initItemStock|秒杀品不存在:{}", itemId);
            return false;
        }
        if (flashItem.getInitialStock() == null) {
            logger.info("initItemStock|秒杀品未设置库存:{}", itemId);
            return false;
        }
        String itemStockKey = getItemStockKey(itemId);
        DistributedLock lock = distributedLockFactoryService.getDistributedLock("ITEM_STOCK_LOCK_" + itemId);
        try {
            boolean isLockSuccess = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                logger.info("initItemStock|初始化库存时获取锁失败:{}", itemId);
                return false;
            }
            List<String> keys = new ArrayList<>();
            keys.add(itemStockKey);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INIT_ITEM_STOCK_LUA, Long.class);
            Long result = (Long) redisCacheService.getRedisTemplate().execute(redisScript, keys, flashItem.getAvailableStock());
            if (result == null) {
                logger.info("initItemStock|秒杀品库存初始化失败:{},{},{},{}", result, itemId, itemStockKey, flashItem.getInitialStock());
                return false;
            }
            if (result == -1) {
                logger.info("initItemStock|秒杀品库存初始化忽略，库存已经存在:{},{},{},{}", result, itemId, itemStockKey, flashItem.getInitialStock());
                return true;
            }
            if (result == 1) {
                logger.info("initItemStock|秒杀品库存初始化完成:{},{},{},{}", result, itemId, itemStockKey, flashItem.getInitialStock());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("initItemStock|秒杀品库存初始化错误:{},{},{}", itemId, itemStockKey, flashItem.getInitialStock(), e);
            return false;
        }
    }

    @Override
    public boolean decreaseItemStock(Long userId, Long itemId, Integer quantity) {
        if (itemId == null || quantity == null) {
            return false;
        }
        String itemStockKey = getItemStockKey(itemId);
        if (!redisCacheService.hasKey(itemStockKey)) {
            logger.info("decreaseItemStock|秒杀品库存未预热:{}", itemId);
            return false;
        }
        try {
            List<String> keys = new ArrayList<>();
            keys.add(itemStockKey);
            keys.add(getItemStockAlignKey(itemId));
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(DECREASE_ITEM_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                result = (Long) redisCacheService.getRedisTemplate().execute(redisScript, keys, quantity);
                if (result == null) {
                    logger.info("decreaseItemStock|库存扣减失败:{},{},{},{}", result, itemId, itemStockKey, quantity, userId);
                    return false;
                }
                if (result == IN_STOCK_ALIGNING) {
                    logger.info("decreaseItemStock|库存校准中:{},{},{},{}", itemId, itemStockKey, quantity, userId);
                    Thread.sleep(20);
                }
                if (result == -1 || result == -2) {
                    logger.info("decreaseItemStock|库存扣减失败:{},{},{},{}", result, itemId, itemStockKey, quantity, userId);
                    return false;
                }
                if (result == -3) {
                    logger.info("decreaseItemStock|库存扣减失败:{},{},{},{}", result, itemId, itemStockKey, quantity, userId);
                    return false;
                }
                if (result == 1) {
                    logger.info("decreaseItemStock|库存扣减成功:{},{},{},{}", result, itemId, itemStockKey, quantity, userId);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("increaseItemStock|库存增加失败:{},{},{},{}", itemId, itemStockKey, quantity, userId, e);
            return false;
        }
        return false;
    }

    @Override
    public boolean increaseItemStock(Long userId, Long itemId, Integer quantity) {
        if (itemId == null || quantity == null) {
            return false;
        }
        String itemStockKey = getItemStockKey(itemId);
        if (!redisCacheService.hasKey(itemStockKey)) {
            logger.info("increaseItemStock|秒杀品库存未预热:{}", itemId);
            return false;
        }
        try {
            List<String> keys = new ArrayList<>();
            keys.add(itemStockKey);
            keys.add(getItemStockAlignKey(itemId));
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INCREASE_ITEM_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                result = (Long) redisCacheService.getRedisTemplate().execute(redisScript, keys, quantity);
                if (result == null) {
                    logger.info("increaseItemStock|库存增加失败:{},{},{},{}", itemId, itemStockKey, quantity, userId);
                    return false;
                }
                if (result == IN_STOCK_ALIGNING) {
                    logger.info("increaseItemStock|库存校准中:{},{},{},{}", itemId, itemStockKey, quantity, userId);
                    Thread.sleep(20);
                }
                if (result == -1) {
                    logger.info("increaseItemStock|库存增加失败:{},{},{},{}", result, itemId, itemStockKey, quantity, userId);
                    return false;
                }
                if (result == 1) {
                    logger.info("increaseItemStock|库存增加成功:{},{},{},{}", result, itemId, itemStockKey, quantity, userId);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("increaseItemStock|库存增加失败:{},{},{},{}", itemId, itemStockKey, quantity, userId, e);
            return false;
        }
        return false;
    }

    @Override
    public ItemStockCache getAvailableItemStock(Long itemId) {
        ItemStockCache itemStockCache = itemStockLocalCache.getIfPresent(itemId);
        if (itemStockCache != null) {
            return itemStockCache;
        }
        Integer availableStock = distributedCacheService.getObject(getItemStockKey(itemId), Integer.class);
        itemStockCache = new ItemStockCache().with(availableStock);
        itemStockLocalCache.put(itemId, itemStockCache);
        logger.info("Item stock local cache was updated:{}", itemId);
        return itemStockCache;
    }

    @Override
    public boolean alignItemStocks(Long itemId, Integer availableStock) {
        if (itemId == null || availableStock == null) {
            return false;
        }

        List<String> keys = new ArrayList<>();
        keys.add(getItemStockAlignKey(itemId));
        keys.add(getItemStockKey(itemId));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(ALIGN_ITEM_STOCK_LUA, Long.class);
        Long result = (Long) redisCacheService.getRedisTemplate().execute(redisScript, keys, availableStock);
        if (result == null) {
            logger.info("alignItemStocks|库存校准失败:{},{}", itemId, availableStock);
            return false;
        }
        if (result == 1) {
            logger.info("alignItemStocks|库存校准成功:{},{},{}", result, itemId, availableStock);
            return true;
        }
        return false;
    }

    private String getItemStockKey(Long itemId) {
        return ITEM_STOCK_KEY + itemId;
    }

    private String getItemStockAlignKey(Long itemId) {
        return ITEM_STOCK_ALIGN_LOCK_KEY + itemId;
    }
}
