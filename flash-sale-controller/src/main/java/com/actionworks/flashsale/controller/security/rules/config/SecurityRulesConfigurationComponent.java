package com.actionworks.flashsale.controller.security.rules.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

@RefreshScope
@Configuration
public class SecurityRulesConfigurationComponent implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(SecurityRulesConfigurationComponent.class);

    @Resource
    private NacosConfigManager nacosConfigManager;

    private SecurityRulesConfiguration securityRulesConfiguration;

    public Rule getPathRule(String servletPath) {
        if (securityRulesConfiguration == null) {
            return new Rule();
        }
        return securityRulesConfiguration.getPathRule(servletPath);
    }

    public Rule getAccountRule() {
        if (securityRulesConfiguration == null) {
            return new Rule();
        }
        return securityRulesConfiguration.getAccountRule();
    }

    public Rule getIpRule() {
        if (securityRulesConfiguration == null) {
            return new Rule();
        }
        return securityRulesConfiguration.getIpRule();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        nacosConfigManager.getConfigService().addListener("FLASH-SALE-SECURITY-RULES-CONFIGURATION", "THOUGHTS-BETA-GROUP", new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                logger.info("receiveSecurityRulesConfiguration|接收风控动态配置|{}", configInfo);
                securityRulesConfiguration = JSON.parseObject(configInfo, SecurityRulesConfiguration.class);
            }
        });
    }
}
