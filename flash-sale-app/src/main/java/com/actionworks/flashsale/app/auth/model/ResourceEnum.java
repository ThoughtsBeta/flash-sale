package com.actionworks.flashsale.app.auth.model;

public enum ResourceEnum {
    FLASH_ITEM_CREATE("FLASH_ITEM_CREATE", "创建秒杀品"),
    FLASH_ITEM_OFFLINE("FLASH_ITEM_OFFLINE", "下线秒杀品"),
    FLASH_ITEMS_GET("FLASH_ITEMS_GET", "秒杀品集合获取"),
    FLASH_ITEM_GET("FLASH_ITEM_GET", "获取指定秒杀品");

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
