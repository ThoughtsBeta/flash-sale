package com.actionworks.flashsale.app.model.builder;

import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import org.springframework.beans.BeanUtils;

public class PlaceOrderTaskBuilder {
    public static PlaceOrderTask with(Long userId, FlashPlaceOrderCommand flashPlaceOrderCommand) {
        if (flashPlaceOrderCommand == null) {
            return null;
        }
        PlaceOrderTask placeOrderTask = new PlaceOrderTask();
        BeanUtils.copyProperties(flashPlaceOrderCommand, placeOrderTask);
        placeOrderTask.setUserId(userId);
        return placeOrderTask;
    }
}
