package com.actionworks.flashsale.controller.security.rules;

import com.actionworks.flashsale.app.auth.AuthorizationService;
import com.actionworks.flashsale.app.auth.model.AuthResult;
import com.actionworks.flashsale.controller.security.rules.config.SecurityRulesConfigurationComponent;
import com.actionworks.flashsale.security.SlidingWindowLimitService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public abstract class SecurityRuleChainServiceBase {
    private static final Logger logger = LoggerFactory.getLogger(SecurityRuleChainServiceBase.class);

    @Resource
    protected SlidingWindowLimitService slidingWindowLimitService;
    @Resource
    protected AuthorizationService authorizationService;
    @Resource
    protected SecurityRulesConfigurationComponent securityRulesConfigurationComponent;

    @PostConstruct
    public void init() {
        logger.info("securityService|{}已初始化", getName());
    }

    protected Long getUserId(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        AuthResult authResult = authorizationService.auth(token);
        if (authResult.isSuccess()) {
            return authResult.getUserId();
        }
        return null;
    }

    public abstract String getName();
}
