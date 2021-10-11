package com.actionworks.flashsale.persistence.model;

import lombok.Data;

import java.util.Date;

@Data
public class BaseDO {
    private Long id;
    private Date createTime;
    private Date modifiedTime;
}
