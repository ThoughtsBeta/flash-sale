package com.actionworks.flashsale.app.model.enums;

public enum ArrangementMode {
    TOTAL(1),
    INCREMENTAL(2);

    private final Integer mode;

    ArrangementMode(Integer mode) {
        this.mode = mode;
    }

    public static boolean isTotalAmountMode(Integer mode) {
        return TOTAL.mode.equals(mode);
    }

    public static boolean isIncrementalAmountMode(Integer mode) {
        return INCREMENTAL.mode.equals(mode);
    }

    public int getMode() {
        return mode;
    }
}
