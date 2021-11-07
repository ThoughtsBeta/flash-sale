package com.actionworks.flashsale.app.model.dto;

import com.actionworks.flashsale.domain.model.enums.FlashItemStatus;
import lombok.Data;

import java.util.Date;

@Data
public class FlashItemDTO {
    /**
     * 秒杀品ID
     */
    private Long id;
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
    private Date startTime;
    /**
     * 秒杀结束时间
     */
    private Date endTime;
    /**
     * 秒杀状态
     */
    private Integer status;
    /**
     * 数据版本号
     */
    private Long version;

    /**
     * 当前秒杀品秒杀是否出于售卖状态
     */
    public boolean isOnSale() {
        if (!FlashItemStatus.isOnline(status)) {
            return false;
        }
        if (startTime == null || endTime == null) {
            return false;
        }
        Date now = new Date();
        return (startTime.equals(now) || startTime.before(now)) && endTime.after(now);
    }
}
