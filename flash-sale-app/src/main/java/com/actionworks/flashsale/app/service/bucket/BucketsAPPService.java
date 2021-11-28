package com.actionworks.flashsale.app.service.bucket;

import com.actionworks.flashsale.app.model.command.BucketsArrangementCommand;
import com.actionworks.flashsale.app.model.dto.StockBucketSummaryDTO;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;

public interface BucketsAPPService {
    AppSimpleResult arrangeStockBuckets(Long userId, Long itemId, BucketsArrangementCommand arrangementCommand);

    AppSimpleResult<StockBucketSummaryDTO> getStockBucketsSummary(Long userId, Long itemId);
}
