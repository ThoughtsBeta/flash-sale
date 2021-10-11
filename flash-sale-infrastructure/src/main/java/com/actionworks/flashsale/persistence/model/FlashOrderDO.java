package com.actionworks.flashsale.persistence.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlashOrderDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String itemTitle;
    private Long flashPrice;
    private Long activityId;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
}
