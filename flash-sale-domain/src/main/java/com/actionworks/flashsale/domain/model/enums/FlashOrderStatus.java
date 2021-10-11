package com.actionworks.flashsale.domain.model.enums;

public enum FlashOrderStatus {
    CREATED(1),
    PAID(2),
    CANCELED(0),
    DELETED(-1);

    private final Integer code;

    FlashOrderStatus(Integer code) {
        this.code = code;
    }

    public static boolean isCancled(Integer status) {
        return CANCELED.getCode().equals(status);
    }

    public static boolean isDeleted(Integer status) {
        return DELETED.getCode().equals(status);
    }

    public Integer getCode() {
        return code;
    }
}
