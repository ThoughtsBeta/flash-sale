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
import static com.actionworks.flashsale.util.IPUtil.getIpAddr;

@Component
public class IPRuleChainService extends SecurityRuleChainServiceBase  implements SecurityRuleChainService {
    private static final Logger logger = LoggerFactory.getLogger(IPRuleChainService.class);

    @Override
    public boolean run(HttpServletRequest request, HttpServletResponse response) {
        Rule rule = securityRulesConfigurationComponent.getIpRule();
        if (!rule.isEnable()) {
            return true;
        }
        try {
            String clientIp = getIpAddr(request);
            boolean result = slidingWindowLimitService.pass(clientIp, rule.getWindowPeriod(), rule.getWindowSize());
            if (!result) {
                ExceptionResponse exceptionResponse = new ExceptionResponse()
                        .setErrorCode(LIMIT_BLOCK.getCode())
                        .setErrorMessage(LIMIT_BLOCK.getDesc());
                response.setContentType(MediaType.APPLICATION_JSON.getType());
                response.setCharacterEncoding("utf-8");
                response.getWriter().write(JSON.toJSONString(exceptionResponse));
                response.getWriter().close();
                logger.info("ipLimit|IP被限制|{}", clientIp);
                return false;
            }
        } catch (Exception e) {
            logger.error("ipLimit|IP限制异常|{}", e);
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "IP防护服务";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
