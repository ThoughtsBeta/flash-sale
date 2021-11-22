package com.actionworks.flashsale.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockBucketDTO {
    private Integer serialNo;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer status;
}
