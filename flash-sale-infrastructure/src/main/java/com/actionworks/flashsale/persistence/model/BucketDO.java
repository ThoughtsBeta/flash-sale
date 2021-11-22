package com.actionworks.flashsale.persistence.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class BucketDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer serialNo;
    private Integer status;
}
