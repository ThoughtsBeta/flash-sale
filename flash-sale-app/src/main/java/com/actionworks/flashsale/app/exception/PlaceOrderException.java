package com.actionworks.flashsale.app.exception;

public class PlaceOrderException extends RuntimeException {
    public PlaceOrderException(AppErrorCode appErrorCode) {
        super(appErrorCode.getErrDesc());
    }
}
