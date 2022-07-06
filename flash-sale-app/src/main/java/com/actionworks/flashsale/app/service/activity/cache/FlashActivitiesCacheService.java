package com.actionworks.flashsale.app.service.activity.cache;

import com.actionworks.flashsale.app.service.activity.cache.model.FlashActivitiesCache;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.actionworks.flashsale.app.model.constants.CacheConstants.ACTIVITIES_CACHE_KEY;
import static com.actionworks.flashsale.app.model.constants.CacheConstants.FIVE_MINUTES;
import static com.actionworks.flashsale.util.StringUtil.link;

@Service
public class FlashActivitiesCacheService {
    private final static Logger logger = LoggerFactory.getLogger(FlashActivitiesCacheService.class);
    private final static Cache<Integer, FlashActivitiesCache> flashActivitiesLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static final String UPDATE_ACTIVITIES_CACHE_LOCK_KEY = "UPDATE_ACTIVITIES_CACHE_LOCK_KEY";
    private final Lock localCacleUpdatelock = new ReentrantLock();

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
                logger.info("activitiesCache|命中本地缓存|{}", pageNumber);
                return flashActivityCache;
            }
            if (version.equals(flashActivityCache.getVersion()) || version < flashActivityCache.getVersion()) {
                logger.info("activitiesCache|命中本地缓存|{},{}", pageNumber, version);
                return flashActivityCache;
            }
            if (version > (flashActivityCache.getVersion())) {
                return getLatestDistributedCache(pageNumber);
            }
        }
        return getLatestDistributedCache(pageNumber);
    }

    private FlashActivitiesCache getLatestDistributedCache(Integer pageNumber) {
        logger.info("activitiesCache|读取远程缓存|{}", pageNumber);
        FlashActivitiesCache distributedFlashActivityCache = distributedCacheService.getObject(buildActivityCacheKey(pageNumber), FlashActivitiesCache.class);
        if (distributedFlashActivityCache == null) {
            distributedFlashActivityCache = tryToUpdateActivitiesCacheByLock(pageNumber);
        }
        if (distributedFlashActivityCache != null && !distributedFlashActivityCache.isLater()) {
            boolean isLockSuccess = localCacleUpdatelock.tryLock();
            if (isLockSuccess) {
                try {
                    flashActivitiesLocalCache.put(pageNumber, distributedFlashActivityCache);
                    logger.info("activitiesCache|本地缓存已更新|{}", pageNumber);
                } finally {
                    localCacleUpdatelock.unlock();
                }
            }
        }
        return distributedFlashActivityCache;
    }

    public FlashActivitiesCache tryToUpdateActivitiesCacheByLock(Integer pageNumber) {
        logger.info("activitiesCache|更新远程缓存|{}", pageNumber);
        DistributedLock lock = distributedLockFactoryService.getDistributedLock(UPDATE_ACTIVITIES_CACHE_LOCK_KEY);
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new FlashActivitiesCache().tryLater();
            }
            PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
            PageResult<FlashActivity> flashActivityPageResult = flashActivityDomainService.getFlashActivities(pagesQueryCondition);
            FlashActivitiesCache flashActivitiesCache;
            if (flashActivityPageResult == null) {
                flashActivitiesCache = new FlashActivitiesCache().notExist();
            } else {
                flashActivitiesCache = new FlashActivitiesCache()
                        .setTotal(flashActivityPageResult.getTotal())
                        .setFlashActivities(flashActivityPageResult.getData())
                        .setVersion(System.currentTimeMillis());
            }
            distributedCacheService.put(buildActivityCacheKey(pageNumber), JSON.toJSONString(flashActivitiesCache), FIVE_MINUTES);
            logger.info("activitiesCache|远程缓存已更新|{}", pageNumber);
            return flashActivitiesCache;
        } catch (InterruptedException e) {
            logger.error("activitiesCache|远程缓存更新失败", e);
            return new FlashActivitiesCache().tryLater();
        } finally {
            lock.unlock();
        }
    }

    private String buildActivityCacheKey(Integer pageNumber) {
        return link(ACTIVITIES_CACHE_KEY, pageNumber);
    }
}
