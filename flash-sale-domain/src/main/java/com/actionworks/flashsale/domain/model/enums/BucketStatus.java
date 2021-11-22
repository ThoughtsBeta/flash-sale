package com.actionworks.flashsale.domain.model.enums;

public enum BucketStatus {
    ENABLED(1),
    DISABLED(0);

    private final Integer code;

    BucketStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
