package com.actionworks.flashsale.controller.security.rules;

import com.actionworks.flashsale.controller.security.rules.config.Rule;
import com.actionworks.flashsale.security.SecurityRuleChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AccountRuleChainService extends SecurityRuleChainServiceBase implements SecurityRuleChainService {
    private static final Logger logger = LoggerFactory.getLogger(AccountRuleChainService.class);

    @Override
    public boolean run(HttpServletRequest request, HttpServletResponse response) {
        Rule rule = securityRulesConfigurationComponent.getAccountRule();
        if (!rule.isEnable()) {
            return true;
        }
        try {
            // 可在此处调用大数据接口或黑名单接口验证账号
            return true;
        } catch (Exception e) {
            logger.error("accountLimit|IP限制异常|{}", e);
            return false;
        }
    }

    @Override
    public String getName() {
        return "账号安全服务";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
