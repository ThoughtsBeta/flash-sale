package com.actionworks.flashsale.lock.redisson;

import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockService implements DistributedLockFactoryService {
    private final Logger logger = LoggerFactory.getLogger(RedissonLockService.class);

    @Resource
    private RedissonClient redissonClient;


    @Override
    public DistributedLock getDistributedLock(String key) {
        RLock rLock = redissonClient.getLock(key);

        return new DistributedLock() {

            @Override
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                boolean isLockSuccess = rLock.tryLock(waitTime, leaseTime, unit);
                logger.info("{} get lock result:{}", key, isLockSuccess);
                return isLockSuccess;
            }

            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                rLock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                if (isLocked() && isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }

            @Override
            public boolean isLocked() {
                return rLock.isLocked();
            }

            @Override
            public boolean isHeldByThread(long threadId) {
                return rLock.isHeldByThread(threadId);
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return rLock.isHeldByCurrentThread();
            }
        };
    }
}
