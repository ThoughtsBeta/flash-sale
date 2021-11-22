package com.actionworks.flashsale.app.service.bucket;

import com.actionworks.flashsale.app.model.dto.StockBucketSummaryDTO;

public interface BucketsArrangementService {
    void arrangeStockBuckets(Long itemId, Integer stocksAmount, Integer bucketsAmount, Integer assignmentMode);

    StockBucketSummaryDTO queryStockBucketsSummary(Long itemId);
}
