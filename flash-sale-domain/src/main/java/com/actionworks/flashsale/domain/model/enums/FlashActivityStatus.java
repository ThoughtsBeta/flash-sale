package com.actionworks.flashsale.domain.model.enums;

public enum FlashActivityStatus {
    PUBLISHED(0),
    ONLINE(1),
    OFFLINE(-1);

    private final Integer code;

    FlashActivityStatus(Integer code) {
        this.code = code;
    }

    public static boolean isOffline(Integer status) {
        return OFFLINE.getCode().equals(status);
    }

    public static boolean isOnline(Integer status) {
        return ONLINE.getCode().equals(status);
    }

    public Integer getCode() {
        return code;
    }
}
