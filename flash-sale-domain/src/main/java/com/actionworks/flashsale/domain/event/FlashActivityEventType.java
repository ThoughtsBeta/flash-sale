package com.actionworks.flashsale.domain.event;

public enum FlashActivityEventType {
    PUBLISHED(0),
    ONLINE(1),
    OFFLINE(2),
    MODIFIED(3);

    private final Integer code;

    FlashActivityEventType(Integer code) {
        this.code = code;
    }
}
