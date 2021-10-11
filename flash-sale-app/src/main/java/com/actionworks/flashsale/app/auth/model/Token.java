package com.actionworks.flashsale.app.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Token {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 令牌失效时间
     */
    private String expireDate;
}
