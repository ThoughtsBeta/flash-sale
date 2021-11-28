package com.actionworks.flashsale.controller.resource;

import com.actionworks.flashsale.app.service.bucket.BucketsAPPService;
import com.actionworks.flashsale.app.model.command.BucketsArrangementCommand;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.app.model.dto.StockBucketSummaryDTO;
import com.actionworks.flashsale.controller.model.builder.BucketsBuilder;
import com.actionworks.flashsale.controller.model.builder.ResponseBuilder;
import com.actionworks.flashsale.controller.model.request.BucketsArrangementRequest;
import com.alibaba.cola.dto.SingleResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@ConditionalOnProperty(name = "place_order_type", havingValue = "buckets", matchIfMissing = true)
public class BucketsStockController {

    @Resource
    private BucketsAPPService bucketsAPPService;

    @PostMapping(value = "/items/{itemId}/buckets")
    public SingleResponse arrangeStockBuckets(@RequestAttribute Long userId, @PathVariable Long itemId, @RequestBody BucketsArrangementRequest bucketsArrangementRequest) {
        BucketsArrangementCommand bucketsArrangementCommand = BucketsBuilder.toCommand(bucketsArrangementRequest);
        AppSimpleResult arrangementResult = bucketsAPPService.arrangeStockBuckets(userId, itemId, bucketsArrangementCommand);
        if (!arrangementResult.isSuccess()) {
            return SingleResponse.buildFailure(arrangementResult.getCode(), arrangementResult.getMessage());
        }
        return SingleResponse.buildSuccess();
    }

    @GetMapping(value = "/items/{itemId}/buckets")
    public SingleResponse<StockBucketSummaryDTO> getBuckets(@RequestAttribute Long userId, @PathVariable Long itemId) {
        AppSimpleResult<StockBucketSummaryDTO> bucketsSummaryResult = bucketsAPPService.getStockBucketsSummary(userId, itemId);
        if (!bucketsSummaryResult.isSuccess() || bucketsSummaryResult.getData() == null) {
            return ResponseBuilder.withSingle(bucketsSummaryResult);
        }
        return SingleResponse.of(bucketsSummaryResult.getData());
    }
}
