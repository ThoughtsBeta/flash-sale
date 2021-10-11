package com.actionworks.flashsale.controller.model.response;

import lombok.Data;

import java.util.Date;

@Data
public class FlashActivityResponse {
    /**
     * 活动ID
     */
    private Long Id;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动开始时间
     */
    private Date startTime;
    /**
     * 活动结束时间
     */
    private Date endTime;
    /**
     * 活动状态
     */
    private Integer status;
    /**
     * 活动描述
     */
    private String activityDesc;

    /**
     * 数据版本
     */
    private Long version;
}
