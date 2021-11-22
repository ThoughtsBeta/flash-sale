package com.actionworks.flashsale.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockDeduction {
    private Long itemId;
    private Integer quantity;
    private Long userId;
    private Integer serialNo;

    public boolean validate() {
        return itemId != null && quantity != null && quantity > 0 && userId != null;
    }
}
