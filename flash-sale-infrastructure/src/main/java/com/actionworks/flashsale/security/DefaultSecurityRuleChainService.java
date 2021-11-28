package com.actionworks.flashsale.security;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DefaultSecurityRuleChainService implements SecurityRuleChainService {
    @Override
    public boolean run(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
