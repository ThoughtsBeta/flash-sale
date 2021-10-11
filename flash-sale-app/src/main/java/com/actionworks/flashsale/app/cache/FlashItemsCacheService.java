package com.actionworks.flashsale.app.cache;

import com.actionworks.flashsale.app.cache.model.FlashItemsCache;
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

import static com.actionworks.flashsale.app.cache.model.CacheConstatants.CACHE_ONLINE_ITEMS;
import static com.actionworks.flashsale.app.cache.model.CacheConstatants.FIVE_MINUTES;

@Service
public class FlashItemsCacheService {
    private final static Logger logger = LoggerFactory.getLogger(FlashItemsCacheService.class);
    private final static Cache<Long, FlashItemsCache> flashItemsLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();

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
                logger.info("Items local cache was hit {}", activityId);
                return flashItemCache;
            }
            if (version.equals(flashItemCache.getVersion()) || version < flashItemCache.getVersion()) {
                logger.info("Item local cache was hit {},{}", activityId, version);
                return flashItemCache;
            }
            if (version > (flashItemCache.getVersion())) {
                return tryToUpdateItemsCacheByLock(activityId);
            }
        }
        FlashItemsCache distributedCachedFlashItem = distributedCacheService.getObject(buildItemCacheKey(activityId), FlashItemsCache.class);
        if (distributedCachedFlashItem == null) {
            distributedCachedFlashItem = tryToUpdateItemsCacheByLock(activityId);
        }
        if (distributedCachedFlashItem.isExist()) {
            flashItemsLocalCache.put(activityId, distributedCachedFlashItem);
            logger.info("Item local cache was updated:{}", activityId);
        }
        return distributedCachedFlashItem;
    }

    public FlashItemsCache tryToUpdateItemsCacheByLock(Long activityId) {
        DistributedLock lock = distributedLockFactoryService.getDistributedLock("UPDATE_ITEMS_CACHE_LOCK");
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new FlashItemsCache().tryLater();
            }
            PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
            pagesQueryCondition.setActivityId(activityId);
            pagesQueryCondition.setStatus(FlashItemStatus.ONLINE.getCode());
            PageResult<FlashItem> flashItemPageResult = flashItemDomainService.getFlashItems(pagesQueryCondition);
            if (flashItemPageResult == null) {
                return new FlashItemsCache().notExist();
            }
            FlashItemsCache flashItemCache = new FlashItemsCache()
                    .setTotal(flashItemPageResult.getTotal())
                    .setFlashItems(flashItemPageResult.getData())
                    .setVersion(System.currentTimeMillis());
            distributedCacheService.put(buildItemCacheKey(activityId), JSON.toJSONString(flashItemCache), FIVE_MINUTES);
            logger.info("Items distributed cache was updated:{}", activityId);

            flashItemsLocalCache.put(activityId, flashItemCache);
            logger.info("Items local cache was updated:{}", activityId);
            return flashItemCache;
        } catch (InterruptedException e) {
            logger.warn("UPDATE_ITEMS_CACHE_LOCK was interrupted.", e);
            return new FlashItemsCache().tryLater();
        } finally {
            lock.forceUnlock();
        }
    }

    private String buildItemCacheKey(Long activityId) {
        return CACHE_ONLINE_ITEMS + activityId;
    }
}
