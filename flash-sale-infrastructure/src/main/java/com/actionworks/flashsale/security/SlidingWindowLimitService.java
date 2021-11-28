package com.actionworks.flashsale.security;

public interface SlidingWindowLimitService {
    /**
     * @param userActionKey 用户及行为标识
     * @param period        限流周期，单位毫秒
     * @param size          滑动窗口大小
     * @return 是否通过
     */
    boolean pass(String userActionKey, int period, int size);
}
