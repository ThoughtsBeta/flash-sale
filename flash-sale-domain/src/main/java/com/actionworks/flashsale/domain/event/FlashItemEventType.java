package com.actionworks.flashsale.domain.event;

public enum FlashItemEventType {
    PUBLISHED(0),
    ONLINE(1),
    OFFLINE(2);

    private final Integer code;

    FlashItemEventType(Integer code) {
        this.code = code;
    }
}
