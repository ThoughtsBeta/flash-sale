package com.actionworks.flashsale.app.security;

import org.springframework.stereotype.Service;

@Service
public class DefaultSecurityService implements SecurityService {
    @Override
    public boolean inspectRisksByPolicy(Long userId) {
        return true;
    }
}
