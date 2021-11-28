package com.actionworks.flashsale.controller.security.rules.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PathRule extends Rule {
    private List<Rule> urlPaths;
}
