package com.actionworks.flashsale.app.service.placeorder.queued;

import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.actionworks.flashsale.app.model.enums.OrderTaskStatus;
import com.actionworks.flashsale.app.model.result.OrderTaskSubmitResult;

public interface PlaceOrderTaskService {

    OrderTaskSubmitResult submit(PlaceOrderTask placeOrderTask);

    void updateTaskHandleResult(String placeOrderTaskId, boolean result);

    OrderTaskStatus getTaskStatus(String placeOrderTaskId);
}
