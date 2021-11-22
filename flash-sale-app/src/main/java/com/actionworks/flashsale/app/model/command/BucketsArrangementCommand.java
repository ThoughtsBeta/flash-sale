package com.actionworks.flashsale.app.model.command;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BucketsArrangementCommand {
    private Integer totalStocksAmount;
    private Integer bucketsQuantity;
    private Integer arrangementMode;
}
