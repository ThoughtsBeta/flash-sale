package com.actionworks.flashsale.app.service.placeorder;

import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;

public interface PlaceOrderService {
    PlaceOrderResult doPlaceOrder(Long userId, FlashPlaceOrderCommand placeOrderCommand);
}
