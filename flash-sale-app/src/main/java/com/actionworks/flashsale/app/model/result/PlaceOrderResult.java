package com.actionworks.flashsale.app.model.result;

import com.actionworks.flashsale.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlaceOrderResult {
    private boolean success;
    private String code;
    private String message;
    private String placeOrderTaskId;
    private Long orderId;

    public static PlaceOrderResult ok(String placeOrderTaskId) {
        return new PlaceOrderResult()
                .setSuccess(true)
                .setPlaceOrderTaskId(placeOrderTaskId);
    }

    public static PlaceOrderResult ok(Long orderId) {
        return new PlaceOrderResult()
                .setSuccess(true)
                .setOrderId(orderId);
    }

    public static PlaceOrderResult failed(String code, String message) {
        return new PlaceOrderResult()
                .setSuccess(false)
                .setCode(code)
                .setMessage(message);
    }

    public static PlaceOrderResult failed(AppErrorCode appErrorCode) {
        return new PlaceOrderResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }

    public static PlaceOrderResult ok() {
        return new PlaceOrderResult()
                .setSuccess(true);
    }
}
