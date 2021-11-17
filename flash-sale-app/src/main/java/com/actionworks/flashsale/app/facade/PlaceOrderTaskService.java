package com.actionworks.flashsale.app.facade;

import com.actionworks.flashsale.app.model.OrderTaskStatus;
import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.actionworks.flashsale.app.model.result.OrderTaskSubmitResult;

public interface PlaceOrderTaskService {

    OrderTaskSubmitResult submit(PlaceOrderTask placeOrderTask);

    void updateTaskHandleResult(String placeOrderTaskId, boolean result);

    OrderTaskStatus getTaskStatus(String placeOrderTaskId);
}
