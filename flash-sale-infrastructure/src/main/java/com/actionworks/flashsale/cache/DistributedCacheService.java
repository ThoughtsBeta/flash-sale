package com.actionworks.flashsale.cache;

import java.util.List;

public interface DistributedCacheService {
    void put(String key, String value);

    boolean put(String key, String value, long expireTime);

    <T> T getObject(String key, Class<T> targetClass);

    String getString(String key);

    <T> List<T> getList(String key, Class<T> targetClass);

    Boolean delete(String key);

    Boolean hasKey(String key);
}
