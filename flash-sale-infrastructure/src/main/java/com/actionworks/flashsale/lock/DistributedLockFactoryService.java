package com.actionworks.flashsale.lock;

public interface DistributedLockFactoryService {
    DistributedLock getDistributedLock(String key);
}
