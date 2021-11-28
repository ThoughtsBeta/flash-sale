package com.actionworks.flashsale.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全规则调用链服务
 */
public interface SecurityRuleChainService {
    /**
     * @param request  请求
     * @param response 响应
     * @return 执行结果
     */
    boolean run(HttpServletRequest request, HttpServletResponse response);

    /**
     * 调用链执行顺序
     *
     * @return 执行顺序
     */
    int getOrder();
}
