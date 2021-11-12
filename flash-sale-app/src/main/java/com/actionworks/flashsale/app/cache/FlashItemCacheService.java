package com.actionworks.flashsale.app.cache;

import com.actionworks.flashsale.app.cache.model.FlashItemCache;
import com.actionworks.flashsale.cache.DistributedCacheService;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.actionworks.flashsale.app.cache.model.CacheConstatants.CACHE_ONLINE_ITEM;
import static com.actionworks.flashsale.app.cache.model.CacheConstatants.FIVE_MINUTES;

@Service
public class FlashItemCacheService {
    private final static Logger logger = LoggerFactory.getLogger(FlashItemCacheService.class);
    private final static Cache<Long, FlashItemCache> flashItemLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static final String UPDATE_ITEM_CACHE_LOCK_KEY = "UPDATE_ITEM_CACHE_LOCK_KEY_";

    @Resource
    private DistributedCacheService distributedCacheService;

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private DistributedLockFactoryService distributedLockFactoryService;

    public FlashItemCache getCachedItem(Long itemId, Long version) {
        FlashItemCache flashItemCache = flashItemLocalCache.getIfPresent(itemId);
        if (flashItemCache != null) {
            if (version == null) {
                logger.info("Item local cache hit {}", itemId);
                return flashItemCache;
            }
            if (version.equals(flashItemCache.getVersion()) || version < flashItemCache.getVersion()) {
                logger.info("Item local cache hit {},{}", itemId, version);
                return flashItemCache;
            }
            if (version > (flashItemCache.getVersion())) {
                return getLatestDistributedCache(itemId);
            }
        }
        return getLatestDistributedCache(itemId);
    }

    private FlashItemCache getLatestDistributedCache(Long itemId) {
        FlashItemCache distributedCachedFlashItem = distributedCacheService.getObject(buildItemCacheKey(itemId), FlashItemCache.class);
        if (distributedCachedFlashItem == null) {
            return tryToUpdateItemCacheByLock(itemId);
        }
        return distributedCachedFlashItem;
    }

    public FlashItemCache tryToUpdateItemCacheByLock(Long itemId) {
        DistributedLock lock = distributedLockFactoryService.getDistributedLock(UPDATE_ITEM_CACHE_LOCK_KEY + itemId);
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new FlashItemCache().tryLater();
            }
            FlashItem flashItem = flashItemDomainService.getFlashItem(itemId);
            if (flashItem == null) {
                return new FlashItemCache().notExist();
            }
            FlashItemCache flashItemCache = new FlashItemCache().with(flashItem).withVersion(System.currentTimeMillis());
            distributedCacheService.put(buildItemCacheKey(itemId), JSON.toJSONString(flashItemCache), FIVE_MINUTES);
            logger.info("Item distributed cache was updated:{}", itemId);

            flashItemLocalCache.put(itemId, flashItemCache);
            logger.info("Item local cache was updated:{}", itemId);
            return flashItemCache;
        } catch (InterruptedException e) {
            logger.warn("UPDATE_ITEM_CACHE_LOCK was interrupted.", e);
            return new FlashItemCache().tryLater();
        } finally {
            lock.forceUnlock();
        }
    }

    private String buildItemCacheKey(Long itemId) {
        return CACHE_ONLINE_ITEM + itemId;
    }
}
