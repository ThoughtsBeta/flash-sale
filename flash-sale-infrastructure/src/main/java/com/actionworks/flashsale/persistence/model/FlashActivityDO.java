package com.actionworks.flashsale.persistence.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlashActivityDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    private String activityName;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Date modifiedTime;
    private Date createTime;
    private String activityDesc;
}
