package com.actionworks.flashsale.controller.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class FlashActivityPublishRequest {
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**
     * 活动结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /**
     * 活动状态
     */
    private Integer status;
    /**
     * 活动描述
     */
    private String activityDesc;
}
