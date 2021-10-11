package com.actionworks.flashsale.app.security;

/**
 * 风控服务
 */
public interface SecurityService {
    /**
     * 根据用户ID及请求上下文和综合行为数据判断当前用户是否合规
     *
     * @param userId 当前用户ID
     */
    boolean inspectRisksByPolicy(Long userId);
}
