package com.actionworks.flashsale.app.cache;

import com.actionworks.flashsale.app.cache.model.FlashActivityCache;
import com.actionworks.flashsale.cache.DistributedCacheService;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import com.actionworks.flashsale.domain.service.FlashActivityDomainService;
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

import static com.actionworks.flashsale.app.cache.model.CacheConstatants.CACHE_ONLINE_ACTIVITIES;
import static com.actionworks.flashsale.app.cache.model.CacheConstatants.CACHE_ONLINE_ACTIVITY;
import static com.actionworks.flashsale.app.cache.model.CacheConstatants.FIVE_MINUTES;

@Service
public class FlashActivityCacheService {
    private final static Logger logger = LoggerFactory.getLogger(FlashActivityCacheService.class);
    private final static Cache<Long, FlashActivityCache> flashActivityLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();

    @Resource
    private DistributedCacheService distributedCacheService;

    @Resource
    private FlashActivityDomainService flashActivityDomainService;

    @Resource
    private DistributedLockFactoryService distributedLockFactoryService;

    public void updateActivitiesCache() {
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition()
                .setPageNumber(1)
                .setPageSize(20);
        PageResult<FlashActivity> onlineActivitiesPageResult = flashActivityDomainService.getFlashActivities(pagesQueryCondition);
        distributedCacheService.put(CACHE_ONLINE_ACTIVITIES, JSON.toJSONString(onlineActivitiesPageResult.getData()), FIVE_MINUTES);
        logger.info("Online activities cache was updated by event.");
    }

    public FlashActivityCache getCachedActivity(Long activityId, Long version) {
        FlashActivityCache flashActivityCache = flashActivityLocalCache.getIfPresent(activityId);
        if (flashActivityCache != null) {
            if (version == null) {
                logger.info("Activity local cache hit {}", activityId);
                return flashActivityCache;
            }
            if (version.equals(flashActivityCache.getVersion()) || version < flashActivityCache.getVersion()) {
                logger.info("Activity local cache hit {},{}", activityId, version);
                return flashActivityCache;
            }
            if (version > (flashActivityCache.getVersion())) {
                return tryToUpdateActivityCacheByLock(activityId);
            }
        }
        FlashActivityCache distributedCachedFlashActivity = distributedCacheService.getObject(buildActivityCacheKey(activityId), FlashActivityCache.class);
        if (distributedCachedFlashActivity == null) {
            distributedCachedFlashActivity = tryToUpdateActivityCacheByLock(activityId);
        }
        if (!distributedCachedFlashActivity.isLater()) {
            flashActivityLocalCache.put(activityId, distributedCachedFlashActivity);
            logger.info("Activity local cache was updated:{}", activityId);
        }
        return distributedCachedFlashActivity;
    }

    public FlashActivityCache tryToUpdateActivityCacheByLock(Long activityId) {
        DistributedLock lock = distributedLockFactoryService.getDistributedLock("UPDATE_ACTIVITY_CACHE_LOCK");
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new FlashActivityCache().tryLater();
            }
            FlashActivity flashActivity = flashActivityDomainService.getFlashActivity(activityId);
            if (flashActivity == null) {
                return new FlashActivityCache().notExist();
            }
            FlashActivityCache flashActivityCache = new FlashActivityCache().with(flashActivity).withVersion(System.currentTimeMillis());
            distributedCacheService.put(buildActivityCacheKey(activityId), JSON.toJSONString(flashActivityCache), FIVE_MINUTES);
            logger.info("Activity distributed cache was updated:{}", activityId);

            flashActivityLocalCache.put(activityId, flashActivityCache);
            logger.info("Activity local cache was updated:{}", activityId);
            return flashActivityCache;
        } catch (InterruptedException e) {
            logger.warn("UPDATE_ACTIVITY_CACHE_LOCK was interrupted.", e);
            return new FlashActivityCache().tryLater();
        } finally {
            lock.forceUnlock();
        }
    }

    private String buildActivityCacheKey(Long activityId) {
        return CACHE_ONLINE_ACTIVITY + activityId;
    }
}
