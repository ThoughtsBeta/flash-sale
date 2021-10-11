package com.actionworks.flashsale.app.cache;

import com.actionworks.flashsale.app.cache.model.FlashActivitiesCache;
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
import static com.actionworks.flashsale.app.cache.model.CacheConstatants.FIVE_MINUTES;

@Service
public class FlashActivitiesCacheService {
    private final static Logger logger = LoggerFactory.getLogger(FlashActivitiesCacheService.class);
    private final static Cache<Integer, FlashActivitiesCache> flashActivitiesLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();

    @Resource
    private DistributedCacheService distributedCacheService;

    @Resource
    private FlashActivityDomainService flashActivityDomainService;

    @Resource
    private DistributedLockFactoryService distributedLockFactoryService;

    public FlashActivitiesCache getCachedActivities(Integer pageNumber, Long version) {
        if (pageNumber == null) {
            pageNumber = 1;
        }
        FlashActivitiesCache flashActivityCache = flashActivitiesLocalCache.getIfPresent(pageNumber);
        if (flashActivityCache != null) {
            if (version == null) {
                logger.info("Activities local cache was hit {}", pageNumber);
                return flashActivityCache;
            }
            if (version.equals(flashActivityCache.getVersion()) || version < flashActivityCache.getVersion()) {
                logger.info("Activity local cache was hit {},{}", pageNumber, version);
                return flashActivityCache;
            }
            if (version > (flashActivityCache.getVersion())) {
                return tryToUpdateActivitiesCacheByLock(pageNumber);
            }
        }
        FlashActivitiesCache distributedCachedFlashActivity = distributedCacheService.getObject(buildActivityCacheKey(pageNumber), FlashActivitiesCache.class);
        if (distributedCachedFlashActivity == null) {
            distributedCachedFlashActivity = tryToUpdateActivitiesCacheByLock(pageNumber);
        }
        if (distributedCachedFlashActivity.isExist()) {
            flashActivitiesLocalCache.put(pageNumber, distributedCachedFlashActivity);
            logger.info("Activity local cache was updated:{}", pageNumber);
        }
        return distributedCachedFlashActivity;
    }

    public FlashActivitiesCache tryToUpdateActivitiesCacheByLock(Integer pageNumber) {
        DistributedLock lock = distributedLockFactoryService.getDistributedLock("UPDATE_ACTIVITIES_CACHE_LOCK");
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new FlashActivitiesCache().tryLater();
            }
            PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
            PageResult<FlashActivity> flashActivityPageResult = flashActivityDomainService.getFlashActivities(pagesQueryCondition);
            if (flashActivityPageResult == null) {
                return new FlashActivitiesCache().notExist();
            }
            FlashActivitiesCache flashActivityCache = new FlashActivitiesCache()
                    .setTotal(flashActivityPageResult.getTotal())
                    .setFlashActivities(flashActivityPageResult.getData())
                    .setVersion(System.currentTimeMillis());
            distributedCacheService.put(buildActivityCacheKey(pageNumber), JSON.toJSONString(flashActivityCache), FIVE_MINUTES);
            logger.info("Activities distributed cache was updated:{}", pageNumber);

            flashActivitiesLocalCache.put(pageNumber, flashActivityCache);
            logger.info("Activities local cache was updated:{}", pageNumber);
            return flashActivityCache;
        } catch (InterruptedException e) {
            logger.warn("UPDATE_ACTIVITIES_CACHE_LOCK was interrupted.", e);
            return new FlashActivitiesCache().tryLater();
        } finally {
            lock.forceUnlock();
        }
    }

    private String buildActivityCacheKey(Integer pageNumber) {
        return CACHE_ONLINE_ACTIVITIES + pageNumber;
    }
}
