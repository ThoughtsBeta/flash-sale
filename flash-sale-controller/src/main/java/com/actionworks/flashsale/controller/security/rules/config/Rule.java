package com.actionworks.flashsale.controller.security.rules.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Rule {
    private boolean enable = false;
    private String path;
    private int windowPeriod;
    private int windowSize;
}
