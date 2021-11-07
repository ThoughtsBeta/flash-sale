package com.actionworks.flashsale.app.mq;

import com.actionworks.flashsale.app.model.PlaceOrderTask;

public interface OrderTaskPostService {
    boolean post(PlaceOrderTask placeOrderTask);
}
