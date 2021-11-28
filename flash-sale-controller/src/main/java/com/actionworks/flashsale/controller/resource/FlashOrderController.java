package com.actionworks.flashsale.controller.resource;

import com.actionworks.flashsale.app.service.order.FlashOrderAppService;
import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.app.model.query.FlashOrdersQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.app.model.result.OrderTaskHandleResult;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;
import com.actionworks.flashsale.controller.model.builder.FlashOrderBuilder;
import com.actionworks.flashsale.controller.model.builder.ResponseBuilder;
import com.actionworks.flashsale.controller.model.request.FlashPlaceOrderRequest;
import com.actionworks.flashsale.controller.model.response.FlashOrderResponse;
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

import static com.actionworks.flashsale.controller.model.builder.FlashOrderBuilder.toFlashOrdersResponse;

@RestController
public class FlashOrderController {

    @Resource
    private FlashOrderAppService flashOrderAppService;

    @PostMapping(value = "/flash-orders")
    @SentinelResource("PlaceOrderResource")
    public SingleResponse<PlaceOrderResult> placeOrder(@RequestAttribute Long userId, @RequestBody FlashPlaceOrderRequest flashPlaceOrderRequest) {
        FlashPlaceOrderCommand placeOrderCommand = FlashOrderBuilder.toCommand(flashPlaceOrderRequest);
        AppSimpleResult<PlaceOrderResult> placeOrderResult = flashOrderAppService.placeOrder(userId, placeOrderCommand);
        if (!placeOrderResult.isSuccess() || placeOrderResult.getData() == null) {
            return ResponseBuilder.withSingle(placeOrderResult);
        }
        return SingleResponse.of(placeOrderResult.getData());
    }

    @GetMapping(value = "/items/{itemId}/flash-orders/{placeOrderTaskId}")
    @SentinelResource("PlaceOrderTask")
    public SingleResponse<OrderTaskHandleResult> getPlaceOrderTaskResult(@RequestAttribute Long userId, @PathVariable Long itemId, @PathVariable String placeOrderTaskId) {
        AppSimpleResult<OrderTaskHandleResult> placeOrderTaskResult = flashOrderAppService.getPlaceOrderTaskResult(userId, itemId, placeOrderTaskId);
        if (!placeOrderTaskResult.isSuccess() || placeOrderTaskResult.getData() == null) {
            return ResponseBuilder.withSingle(placeOrderTaskResult);
        }
        return SingleResponse.of(placeOrderTaskResult.getData());
    }

    @GetMapping(value = "/flash-orders/my")
    public MultiResponse<FlashOrderResponse> myOrders(@RequestAttribute Long userId,
                                                      @RequestParam Integer pageSize,
                                                      @RequestParam Integer pageNumber,
                                                      @RequestParam(required = false) String keyword) {
        FlashOrdersQuery flashOrdersQuery = new FlashOrdersQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);

        AppMultiResult<FlashOrderDTO> flashOrdersResult = flashOrderAppService.getOrdersByUser(userId, flashOrdersQuery);
        if (!flashOrdersResult.isSuccess() || flashOrdersResult.getData() == null) {
            return ResponseBuilder.withMulti(flashOrdersResult);
        }
        return MultiResponse.of(toFlashOrdersResponse(flashOrdersResult.getData()), flashOrdersResult.getTotal());
    }

    @PutMapping(value = "/flash-orders/{orderId}/cancel")
    public Response cancelOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        AppResult appResult = flashOrderAppService.cancelOrder(userId, orderId);
        return ResponseBuilder.with(appResult);
    }
}
