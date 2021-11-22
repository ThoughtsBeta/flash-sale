package com.actionworks.flashsale.app.model.enums;

public enum OrderTaskStatus {
    SUBMITTED(0, "初始提交"),
    SUCCESS(1, "下单成功"),
    FAILED(-1, "下单失败");

    private final Integer status;
    private final String desc;

    OrderTaskStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static OrderTaskStatus findBy(Integer status) {
        if (status == null) {
            return null;
        }
        for (OrderTaskStatus taskStatus : OrderTaskStatus.values()) {
            if (taskStatus.getStatus().equals(status)) {
                return taskStatus;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
