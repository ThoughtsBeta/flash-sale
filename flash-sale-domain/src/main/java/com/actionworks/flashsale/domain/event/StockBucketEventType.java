package com.actionworks.flashsale.domain.event;

public enum StockBucketEventType {
    DISABLED(0),
    ENABLED(1),
    ARRANGED(2);

    public Integer getCode() {
        return code;
    }

    private final Integer code;

    StockBucketEventType(Integer code) {
        this.code = code;
    }
}
