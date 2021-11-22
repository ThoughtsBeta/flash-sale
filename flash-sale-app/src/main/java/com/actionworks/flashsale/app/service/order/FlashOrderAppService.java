package com.actionworks.flashsale.app.service.order;

import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.app.model.query.FlashOrdersQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.app.model.result.OrderTaskHandleResult;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;

public interface FlashOrderAppService {
    AppSimpleResult<PlaceOrderResult> placeOrder(String token, FlashPlaceOrderCommand placeOrderCommand);

    AppSimpleResult<OrderTaskHandleResult> getPlaceOrderTaskResult(String token, Long itemId, String placeOrderTaskId);

    AppMultiResult<FlashOrderDTO> getOrdersByUser(String token, FlashOrdersQuery flashOrdersQuery);

    AppResult cancelOrder(String token, Long orderId);

}
