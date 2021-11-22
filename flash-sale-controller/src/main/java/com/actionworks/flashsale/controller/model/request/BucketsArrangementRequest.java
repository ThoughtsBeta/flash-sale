package com.actionworks.flashsale.controller.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BucketsArrangementRequest {
    private Integer totalStocksAmount;
    private Integer bucketsQuantity;
    private Integer arrangementMode;
}
