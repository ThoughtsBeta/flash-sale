package com.actionworks.flashsale.controller.security.rules;

import com.actionworks.flashsale.controller.exception.ExceptionResponse;
import com.actionworks.flashsale.controller.security.rules.config.Rule;
import com.actionworks.flashsale.security.SecurityRuleChainService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.actionworks.flashsale.controller.constants.ExceptionCode.LIMIT_BLOCK;
import static com.actionworks.flashsale.util.StringUtil.link;

@Component
public class ResourcePathRuleChainService extends SecurityRuleChainServiceBase implements SecurityRuleChainService{
    private static final Logger logger = LoggerFactory.getLogger(ResourcePathRuleChainService.class);

    @Override
    public boolean run(HttpServletRequest request, HttpServletResponse response) {
        Rule rule = securityRulesConfigurationComponent.getPathRule(request.getServletPath());
        if (!rule.isEnable()) {
            return true;
        }
        try {
            Long userId = getUserId(request);
            if (userId != null) {
                String userResourcePath = link(userId, request.getServletPath());
                boolean result = slidingWindowLimitService.pass(userResourcePath, rule.getWindowPeriod(), rule.getWindowSize());
                if (!result) {
                    ExceptionResponse exceptionResponse = new ExceptionResponse()
                            .setErrorCode(LIMIT_BLOCK.getCode())
                            .setErrorMessage(LIMIT_BLOCK.getDesc());
                    response.setContentType(MediaType.APPLICATION_JSON.getType());
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(JSON.toJSONString(exceptionResponse));
                    response.getWriter().close();
                    logger.info("resourcePathLimit|资源路径限制|{}", userResourcePath);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("resourcePathLimit|资源路径限制异常|{}", e);
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "资源安全服务";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
