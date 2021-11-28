package com.actionworks.flashsale.app.exception;

public class BizException extends AppException {
    public BizException(String message) {
        super(message);
    }

    public BizException(AppErrorCode errorCode) {
        super(errorCode.getErrDesc());
    }
}
