package com.actionworks.flashsale.domain.model.entity;

import com.actionworks.flashsale.domain.model.enums.FlashItemStatus;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlashItem implements Serializable {
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
     * 库存是否已经预热
     */
    private Integer stockWarmUp;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public boolean validateParamsForCreate() {
        if (StringUtils.isEmpty(itemTitle)
                || activityId == null
                || initialStock == null || initialStock <= 0
                || availableStock == null || availableStock <= 0
                || availableStock > initialStock
                || originalPrice == null || originalPrice < 0
                || flashPrice == null || flashPrice < 0
                || startTime == null
                || endTime == null || endTime.before(startTime) || endTime.before(new Date())) {
            return false;
        }
        return true;
    }

    public boolean isOnline() {
        return FlashItemStatus.isOnline(status);
    }

    public boolean isInProgress() {
        Date now = new Date();
        return startTime.before(now) && endTime.after(now);
    }
}
