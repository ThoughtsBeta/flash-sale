package com.actionworks.flashsale.controller.model.request;

import lombok.Data;

@Data
public class FlashPlaceOrderRequest {
    /**
     * 订单ID
     */
    private Long id;
    /**
     * 商品ID
     */
    private Long itemId;
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
}
