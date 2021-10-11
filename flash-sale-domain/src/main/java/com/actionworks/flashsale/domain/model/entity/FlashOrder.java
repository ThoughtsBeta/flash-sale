package com.actionworks.flashsale.domain.model.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlashOrder implements Serializable {

    /**
     * 订单ID
     */
    private Long id;
    /**
     * 商品ID
     */
    private Long itemId;
    /**
     * 秒杀品标题
     */
    private String itemTitle;
    /**
     * 秒杀价
     */
    private Long flashPrice;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 下单商品数量
     */
    private Integer quantity;
    /**
     * 总金额
     */
    private Long totalAmount;
    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单创建时间
     */
    private Date createTime;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public boolean validateParamsForCreate() {
        if (itemId == null
                || activityId == null
                || quantity == null || quantity <= 0
                || totalAmount == null || totalAmount < 0) {
            return false;
        }
        return true;
    }
}
