package com.actionworks.flashsale.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class StockBucketSummaryDTO {
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private List<StockBucketDTO> buckets;
}
