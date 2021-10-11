package com.actionworks.flashsale.persistence.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlashItemDO extends BaseDO {
    private static final long serialVersionUID = 1L;
    private String itemTitle;
    private String itemSubTitle;
    private Integer initialStock;
    private Integer availableStock;
    private Integer stockWarmUp;
    private Long originalPrice;
    private Long flashPrice;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Long activityId;
    private String itemDesc;
    private String rules;
}
