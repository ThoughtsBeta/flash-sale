package com.actionworks.flashsale.app.auth.model;

public enum ResourceEnum {
    FLASH_ACTIVITY_CREATE("FLASH_ACTIVITY_CREATE", "创建活动"),
    FLASH_ACTIVITY_MODIFICATION("FLASH_ACTIVITY_MODIFICATION", "活动修改"),

    FLASH_ITEM_CREATE("FLASH_ITEM_CREATE", "创建秒杀品"),
    FLASH_ITEM_MODIFICATION("FLASH_ITEM_MODIFICATION", "秒杀品修改"),

    STOCK_BUCKETS_ARRANGEMENT("STOCK_BUCKETS_ARRANGEMENT", "编排库存分桶"),
    STOCK_BUCKETS_SUMMERY_QUERY("STOCK_BUCKETS_SUMMERY_QUERY", "获取库存分桶");

    private final String code;
    private final String desc;

    ResourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
