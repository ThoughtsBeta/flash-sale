package com.actionworks.flashsale.controller.config;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class AuthRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String> headerParams = new HashMap<>();

    public AuthRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    private HttpServletRequest _getHttpServletRequest() {
        return (HttpServletRequest) super.getRequest();
    }

    @Override
    public String getHeader(String name) {
        String value = this._getHttpServletRequest().getHeader(name);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }
        return headerParams.get(name);
    }

    public void addHeader(String name, String value) {
        this.headerParams.put(name, value);
    }
}
