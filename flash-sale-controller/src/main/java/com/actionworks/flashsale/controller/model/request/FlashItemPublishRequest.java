package com.actionworks.flashsale.controller.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class FlashItemPublishRequest {
    /**
     * 秒杀活动ID
     */
    private Long activityId;

    /**
     * 秒杀品标题
     */
    private String itemTitle;
    /**
     * 秒杀品副标题
     */
    private String itemSubTitle;

    /**
     * 秒杀品介绍
     */
    private String itemDesc;
    /**
     * 初始库存
     */
    private Integer initialStock;
    /**
     * 当前可用库存
     */
    private Integer availableStock;
    /**
     * 原价
     */
    private Long originalPrice;
    /**
     * 秒杀价
     */
    private Long flashPrice;
    /**
     * 秒杀开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**
     * 秒杀结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
