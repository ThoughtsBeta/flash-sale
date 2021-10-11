package com.actionworks.flashsale.cache.redis;

import com.actionworks.flashsale.cache.DistributedCacheService;
import com.actionworks.flashsale.cache.redis.util.ProtoStuffSerializerUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisCacheService implements DistributedCacheService {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void put(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public boolean put(String key, String value, long expireTime) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return false;
        }
        Object result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.setEx(key.getBytes(), expireTime, value.getBytes());
            return true;
        });
        return result != null;
    }

    @Override
    public <T> T getObject(String key, Class<T> targetClass) {
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        try {
            return JSON.parseObject(JSON.toJSONString(result), targetClass);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getString(String key) {
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        return String.valueOf(result);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> targetClass) {
        Object result = redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.get(key.getBytes()));
        if (result == null) {
            return null;
        }
        return ProtoStuffSerializerUtil.deserializeList(String.valueOf(result).getBytes(), targetClass);
    }

    @Override
    public Boolean delete(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return redisTemplate.delete(key);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

}
