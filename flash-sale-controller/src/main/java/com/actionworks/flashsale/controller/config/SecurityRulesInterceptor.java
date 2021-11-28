package com.actionworks.flashsale.controller.config;

import com.actionworks.flashsale.security.SecurityRuleChainService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityRulesInterceptor implements HandlerInterceptor {

    @Resource
    private List<SecurityRuleChainService> securityRuleChainServices;

    private List<SecurityRuleChainService> getSecurityRuleChainServices() {
        if (CollectionUtils.isEmpty(securityRuleChainServices)) {
            return new ArrayList<>();
        }
        return securityRuleChainServices
                .stream()
                .sorted(Comparator.comparing(SecurityRuleChainService::getOrder))
                .collect(Collectors.toList());
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        for (SecurityRuleChainService securityRuleChainService : getSecurityRuleChainServices()) {
            if (!securityRuleChainService.run(request, response)) {
                return false;
            }
        }
        return true;
    }
}
