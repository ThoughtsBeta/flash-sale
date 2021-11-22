package com.actionworks.flashsale.app.model.result;

import com.actionworks.flashsale.app.exception.AppErrorCode;
import com.actionworks.flashsale.app.model.enums.OrderTaskStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderTaskHandleResult {
    private boolean success;
    private OrderTaskStatus orderTaskStatus;
    private Long orderId;
    private String code;
    private String message;

    public static OrderTaskHandleResult failed(AppErrorCode appErrorCode) {
        return new OrderTaskHandleResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }

    public static OrderTaskHandleResult ok(Long orderId) {
        return new OrderTaskHandleResult()
                .setSuccess(true)
                .setOrderTaskStatus(OrderTaskStatus.SUCCESS)
                .setOrderId(orderId);
    }

    public static OrderTaskHandleResult failed(OrderTaskStatus orderTaskStatus) {
        return new OrderTaskHandleResult()
                .setSuccess(false)
                .setOrderTaskStatus(orderTaskStatus);
    }
}
