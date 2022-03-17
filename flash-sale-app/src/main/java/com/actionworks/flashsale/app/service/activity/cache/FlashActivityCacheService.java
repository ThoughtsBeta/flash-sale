package com.actionworks.flashsale.app.service.activity.cache;

import com.actionworks.flashsale.app.service.activity.cache.model.FlashActivityCache;
import com.actionworks.flashsale.cache.DistributedCacheService;
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

import static com.actionworks.flashsale.app.model.constants.CacheConstants.ACTIVITY_CACHE_KEY;
import static com.actionworks.flashsale.app.model.constants.CacheConstants.FIVE_MINUTES;
import static com.actionworks.flashsale.util.StringUtil.link;

@Service
public class FlashActivityCacheService {
    private final static Logger logger = LoggerFactory.getLogger(FlashActivityCacheService.class);
    private final static Cache<Long, FlashActivityCache> flashActivityLocalCache = CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(5).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static final String UPDATE_ACTIVITY_CACHE_LOCK_KEY = "UPDATE_ACTIVITY_CACHE_LOCK_KEY_";
    private final Lock localCacleUpdatelock = new ReentrantLock();

    @Resource
    private DistributedCacheService distributedCacheService;

    @Resource
    private FlashActivityDomainService flashActivityDomainService;

    @Resource
    private DistributedLockFactoryService distributedLockFactoryService;

    public FlashActivityCache getCachedActivity(Long activityId, Long version) {
        FlashActivityCache flashActivityCache = flashActivityLocalCache.getIfPresent(activityId);
        if (flashActivityCache != null) {
            if (version == null) {
                logger.info("activityCache|命中本地缓存|{}", activityId);
                return flashActivityCache;
            }
            if (version.equals(flashActivityCache.getVersion()) || version < flashActivityCache.getVersion()) {
                logger.info("activityCache|命中本地缓存|{}", activityId, version);
                return flashActivityCache;
            }

            if (version > (flashActivityCache.getVersion())) {
                return getLatestDistributedCache(activityId);
            }
        }
        return getLatestDistributedCache(activityId);
    }

    private FlashActivityCache getLatestDistributedCache(Long activityId) {
        logger.info("activityCache|读取远程缓存|{}", activityId);
        FlashActivityCache distributedFlashActivityCache = distributedCacheService.getObject(buildActivityCacheKey(activityId), FlashActivityCache.class);
        if (distributedFlashActivityCache == null) {
            distributedFlashActivityCache = tryToUpdateActivityCacheByLock(activityId);
        }
        if (distributedFlashActivityCache != null && !distributedFlashActivityCache.isLater()) {
            boolean isLockSuccess = localCacleUpdatelock.tryLock();
            if (isLockSuccess) {
                try {
                    flashActivityLocalCache.put(activityId, distributedFlashActivityCache);
                    logger.info("activityCache|本地缓存已更新|{}", activityId);
                } finally {
                    localCacleUpdatelock.unlock();
                }
            }
        }
        return distributedFlashActivityCache;
    }

    public FlashActivityCache tryToUpdateActivityCacheByLock(Long activityId) {
        logger.info("activityCache|更新远程缓存|{}", activityId);
        DistributedLock lock = distributedLockFactoryService.getDistributedLock(UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId);
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new FlashActivityCache().tryLater();
            }
            FlashActivity flashActivity = flashActivityDomainService.getFlashActivity(activityId);
            FlashActivityCache flashActivityCache;
            if (flashActivity == null) {
                flashActivityCache = new FlashActivityCache().notExist();
            } else {
                flashActivityCache = new FlashActivityCache().with(flashActivity).withVersion(System.currentTimeMillis());
            }
            distributedCacheService.put(buildActivityCacheKey(activityId), JSON.toJSONString(flashActivityCache), FIVE_MINUTES);
            logger.info("activityCache|远程缓存已更新|{}", activityId);
            return flashActivityCache;
        } catch (InterruptedException e) {
            logger.error("activityCache|远程缓存更新失败|{}", activityId);
            return new FlashActivityCache().tryLater();
        } finally {
            lock.unlock();
        }
    }

    private String buildActivityCacheKey(Long activityId) {
        return link(ACTIVITY_CACHE_KEY, activityId);
    }
}
