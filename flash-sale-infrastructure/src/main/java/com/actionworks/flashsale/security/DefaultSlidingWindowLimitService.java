package com.actionworks.flashsale.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class DefaultSlidingWindowLimitService implements SlidingWindowLimitService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean pass(String userActionKey, int windowPeriod, int windowSize) {
        long current = System.currentTimeMillis();
        long length = windowPeriod * windowSize;
        long start = current - length;
        long expireTime = length + windowPeriod;
        redisTemplate.opsForZSet().add(userActionKey, String.valueOf(current), current);
        // 移除[0,start]区间内的值
        redisTemplate.opsForZSet().removeRangeByScore(userActionKey, 0, start);
        // 获取窗口内元素个数
        Long count = redisTemplate.opsForZSet().zCard(userActionKey);
        // 过期时间 窗口长度+一个时间间隔
        redisTemplate.expire(userActionKey, expireTime, TimeUnit.MILLISECONDS);
        if (count == null) {
            return false;
        }
        return count <= windowSize;
    }
}
