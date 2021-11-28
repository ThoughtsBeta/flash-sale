package com.actionworks.flashsale.controller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class SecurityAdapter implements WebMvcConfigurer {
    @Resource
    private AuthInterceptor authInterceptor;
    @Resource
    private SecurityRulesInterceptor securityRulesInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        registry.addInterceptor(securityRulesInterceptor).addPathPatterns("/**");
    }
}
