package com.actionworks.flashsale.controller.resource;

import com.actionworks.flashsale.app.model.dto.FlashItemDTO;
import com.actionworks.flashsale.app.model.query.FlashItemsQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.app.service.item.FlashItemAppService;
import com.actionworks.flashsale.controller.model.builder.ResponseBuilder;
import com.actionworks.flashsale.controller.model.request.FlashItemPublishRequest;
import com.actionworks.flashsale.controller.model.response.FlashItemResponse;
import com.actionworks.flashsale.domain.model.enums.FlashItemStatus;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.actionworks.flashsale.controller.model.builder.FlashItemBuilder.toCommand;
import static com.actionworks.flashsale.controller.model.builder.FlashItemBuilder.toFlashItemResponse;
import static com.actionworks.flashsale.controller.model.builder.FlashItemBuilder.toFlashItemsResponse;

@RestController
public class FlashItemController {

    @Resource
    private FlashItemAppService flashItemAppService;

    @PostMapping(value = "/activities/{activityId}/flash-items")
    public Response publishFlashItem(@RequestAttribute Long userId, @PathVariable Long activityId, @RequestBody FlashItemPublishRequest flashItemPublishRequest) {
        AppResult publishResult = flashItemAppService.publishFlashItem(userId, activityId, toCommand(flashItemPublishRequest));
        return ResponseBuilder.with(publishResult);

    }

    @GetMapping(value = "/activities/{activityId}/flash-items")
    @SentinelResource("GetFlashItems")
    public MultiResponse<FlashItemDTO> getFlashItems(@RequestAttribute Long userId,
                                                     @PathVariable Long activityId,
                                                     @RequestParam Integer pageSize,
                                                     @RequestParam Integer pageNumber,
                                                     @RequestParam(required = false) String keyword) {
        FlashItemsQuery flashItemsQuery = new FlashItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        AppMultiResult<FlashItemDTO> flashItemsResult = flashItemAppService.getFlashItems(userId, activityId, flashItemsQuery);
        return ResponseBuilder.withMulti(flashItemsResult);
    }

    @GetMapping(value = "/activities/{activityId}/flash-items/online")
    @SentinelResource("GetOnlineFlashItems")
    public MultiResponse<FlashItemResponse> getOnlineFlashItems(@RequestAttribute Long userId,
                                                                @PathVariable Long activityId,
                                                                @RequestParam Integer pageSize,
                                                                @RequestParam Integer pageNumber,
                                                                @RequestParam(required = false) String keyword) {
        FlashItemsQuery flashItemsQuery = new FlashItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(FlashItemStatus.ONLINE.getCode());
        AppMultiResult<FlashItemDTO> flashItemsResult = flashItemAppService.getFlashItems(userId, activityId, flashItemsQuery);
        if (!flashItemsResult.isSuccess() || flashItemsResult.getData() == null) {
            return ResponseBuilder.withMulti(flashItemsResult);
        }
        return MultiResponse.of(toFlashItemsResponse(flashItemsResult.getData()), flashItemsResult.getTotal());
    }

    @GetMapping(value = "/activities/{activityId}/flash-items/{itemId}")
    @SentinelResource("GetFlashItem")
    public SingleResponse<FlashItemResponse> getFlashItem(@RequestAttribute Long userId,
                                                          @PathVariable Long activityId,
                                                          @PathVariable Long itemId,
                                                          @RequestParam(required = false) Long version) {
        AppSimpleResult<FlashItemDTO> flashItemResult = flashItemAppService.getFlashItem(userId, activityId, itemId, version);
        if (!flashItemResult.isSuccess() || flashItemResult.getData() == null) {
            return ResponseBuilder.withSingle(flashItemResult);
        }
        return SingleResponse.of(toFlashItemResponse(flashItemResult.getData()));
    }

    @PutMapping(value = "/activities/{activityId}/flash-items/{itemId}/online")
    public Response onlineFlashItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        AppResult onlineResult = flashItemAppService.onlineFlashItem(userId, activityId, itemId);
        return ResponseBuilder.with(onlineResult);
    }

    @PutMapping(value = "/activities/{activityId}/flash-items/{itemId}/offline")
    public Response offlineFlashItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        AppResult onlineResult = flashItemAppService.onlineFlashItem(userId, activityId, itemId);
        return ResponseBuilder.with(onlineResult);
    }
}
