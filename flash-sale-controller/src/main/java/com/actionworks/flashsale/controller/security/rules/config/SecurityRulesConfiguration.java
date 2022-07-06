package com.actionworks.flashsale.controller.security.rules.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Data
@Accessors(chain = true)
public class SecurityRulesConfiguration {
    private boolean enable;

    private Rule ipRule;
    private PathRule pathRule;
    private Rule accountRule;

    public Rule getIpRule() {
        if (ipRule == null || !ipRule.isEnable() || !this.isEnable()) {
            return new Rule().setEnable(false);
        }
        return new Rule()
                .setEnable(ipRule.isEnable())
                .setWindowPeriod(ipRule.getWindowPeriod())
                .setWindowSize(ipRule.getWindowSize());
    }

    public Rule getPathRule(String path) {
        if (StringUtils.isEmpty(path) || pathRule == null || CollectionUtils.isEmpty(pathRule.getUrlPaths())) {
            return new Rule().setEnable(false);
        }
        if (!this.isEnable() || !pathRule.isEnable()) {
            return new Rule().setEnable(false);
        }
        for (Rule rule : pathRule.getUrlPaths()) {
            if (StringUtils.isEmpty(rule.getPath())) {
                continue;
            }
            if (isPathMatchTemplate(rule.getPath(), path)) {
                return new Rule()
                        .setEnable(rule.isEnable())
                        .setPath(rule.getPath())
                        .setWindowPeriod(rule.getWindowPeriod() == 0 ? pathRule.getWindowPeriod() : rule.getWindowPeriod())
                        .setWindowSize(rule.getWindowSize() == 0 ? pathRule.getWindowSize() : rule.getWindowSize());
            }
        }
        return new Rule().setEnable(false);
    }

    public Rule getAccountRule() {
        if (accountRule == null || !accountRule.isEnable() || !this.isEnable()) {
            return new Rule().setEnable(false);
        }
        return new Rule().setEnable(accountRule.isEnable());
    }

    private boolean isPathMatchTemplate(String uriTemplate, String path) {
        PathPatternParser parser = new PathPatternParser();
        parser.setMatchOptionalTrailingSeparator(true);
        PathPattern pathPattern = parser.parse(uriTemplate);
        PathContainer pathContainer = toPathContainer(path);
        return pathPattern.matches(pathContainer);
    }

    private PathContainer toPathContainer(String path) {
        if (path == null) {
            return null;
        }
        return PathContainer.parsePath(path);
    }
}
