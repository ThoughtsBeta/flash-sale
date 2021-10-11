package com.actionworks.flashsale.domain.event;

public enum FlashOrderEventType {
    CREATED(0),
    CANCEL(1);

    private final Integer code;

    FlashOrderEventType(Integer code) {
        this.code = code;
    }
}
