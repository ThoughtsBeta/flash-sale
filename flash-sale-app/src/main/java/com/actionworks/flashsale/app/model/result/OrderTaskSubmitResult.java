package com.actionworks.flashsale.app.model.result;

import com.actionworks.flashsale.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderTaskSubmitResult {
    private boolean success;
    private String code;
    private String message;

    public static OrderTaskSubmitResult ok() {
        return new OrderTaskSubmitResult()
                .setSuccess(true);
    }

    public static OrderTaskSubmitResult failed(AppErrorCode appErrorCode) {
        return new OrderTaskSubmitResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }
}
