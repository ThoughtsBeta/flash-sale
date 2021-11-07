package com.actionworks.flashsale.app.service;

import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.app.model.query.FlashOrdersQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSingleResult;
import com.actionworks.flashsale.app.model.result.OrderTaskHandleResult;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;

public interface FlashOrderAppService {
    AppSingleResult<PlaceOrderResult> placeOrder(String token, FlashPlaceOrderCommand placeOrderCommand);

    AppSingleResult<OrderTaskHandleResult> getPlaceOrderTaskResult(String token, Long itemId, String placeOrderTaskId);

    AppMultiResult<FlashOrderDTO> getOrdersByUser(String token, FlashOrdersQuery flashOrdersQuery);

    AppResult cancelOrder(String token, Long orderId);

}
