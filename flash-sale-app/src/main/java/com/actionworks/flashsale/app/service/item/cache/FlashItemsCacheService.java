package com.actionworks.flashsale.app.service.item.cache;

import com.actionworks.flashsale.app.service.item.cache.model.FlashItemsCache;
import com.actionworks.flashsale.cache.DistributedCacheService;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.model.enums.FlashItemStatus;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.actionworks.flashsale.app.model.constants.CacheConstants.FIVE_MINUTES;
import static com.actionworks.flashsale.app.model.constants.CacheConstants.ITEMS_CACHE_KEY;

@Service
public class FlashItemsCacheService {
    private final static Logger logger = LoggerFactory.getLogger(FlashItemsCacheService.class);
    private final static Cache<Long, FlashItemsCache> flashItemsLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static final String UPDATE_ITEMS_CACHE_LOCK_KEY = "UPDATE_ITEMS_CACHE_LOCK_KEY_";
    private final Lock localCacleUpdatelock = new ReentrantLock();

    @Resource
    private DistributedCacheService distributedCacheService;

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private DistributedLockFactoryService distributedLockFactoryService;

    public FlashItemsCache getCachedItems(Long activityId, Long version) {
        FlashItemsCache flashItemCache = flashItemsLocalCache.getIfPresent(activityId);
        if (flashItemCache != null) {
            if (version == null) {
                logger.info("itemsCache|命中本地缓存|{}", activityId);
                return flashItemCache;
            }
            if (version.equals(flashItemCache.getVersion()) || version < flashItemCache.getVersion()) {
                logger.info("itemsCache|命中本地缓存|{}", activityId, version);
                return flashItemCache;
            }
            if (version > (flashItemCache.getVersion())) {
                return getLatestDistributedCache(activityId);
            }
        }
        return getLatestDistributedCache(activityId);
    }

    private FlashItemsCache getLatestDistributedCache(Long activityId) {
        logger.info("itemsCache|读取远程缓存|{}", activityId);
        FlashItemsCache distributedCachedFlashItem = distributedCacheService.getObject(buildItemCacheKey(activityId), FlashItemsCache.class);
        if (distributedCachedFlashItem == null) {
            distributedCachedFlashItem = tryToUpdateItemsCacheByLock(activityId);
        }
        if (distributedCachedFlashItem != null && !distributedCachedFlashItem.isLater()) {
            boolean isLockSuccess = localCacleUpdatelock.tryLock();
            if (isLockSuccess) {
                try {
                    flashItemsLocalCache.put(activityId, distributedCachedFlashItem);
                    logger.info("itemsCache|本地缓存已更新|{}", activityId);
                } finally {
                    localCacleUpdatelock.unlock();
                }
            }
        }

        return distributedCachedFlashItem;
    }

    public FlashItemsCache tryToUpdateItemsCacheByLock(Long activityId) {
        logger.info("itemsCache|更新远程缓存|{}", activityId);
        DistributedLock lock = distributedLockFactoryService.getDistributedLock(UPDATE_ITEMS_CACHE_LOCK_KEY + activityId);
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new FlashItemsCache().tryLater();
            }
            PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
            pagesQueryCondition.setActivityId(activityId);
            pagesQueryCondition.setStatus(FlashItemStatus.ONLINE.getCode());
            PageResult<FlashItem> flashItemPageResult = flashItemDomainService.getFlashItems(pagesQueryCondition);
            FlashItemsCache flashItemsCache;
            if (flashItemPageResult == null) {
                flashItemsCache = new FlashItemsCache().empty();
            } else {
                flashItemsCache = new FlashItemsCache()
                        .setTotal(flashItemPageResult.getTotal())
                        .setFlashItems(flashItemPageResult.getData())
                        .setVersion(System.currentTimeMillis());
            }
            distributedCacheService.put(buildItemCacheKey(activityId), JSON.toJSONString(flashItemsCache), FIVE_MINUTES);
            logger.info("itemsCache|远程缓存已更新|{}", activityId);
            return flashItemsCache;
        } catch (Exception e) {
            logger.error("itemsCache|远程缓存更新失败|{}", activityId);
            return new FlashItemsCache().tryLater();
        } finally {
            lock.unlock();
        }
    }

    private String buildItemCacheKey(Long activityId) {
        return ITEMS_CACHE_KEY + activityId;
    }
}
