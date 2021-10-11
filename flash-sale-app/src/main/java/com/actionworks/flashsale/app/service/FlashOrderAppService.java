package com.actionworks.flashsale.app.service;

import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.app.model.query.FlashOrdersQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;

public interface FlashOrderAppService {
    AppResult placeOrder(String token, FlashPlaceOrderCommand placeOrderCommand);

    AppMultiResult<FlashOrderDTO> getOrdersByUser(String token, FlashOrdersQuery flashOrdersQuery);

    AppResult cancelOrder(String token, Long orderId);
}
