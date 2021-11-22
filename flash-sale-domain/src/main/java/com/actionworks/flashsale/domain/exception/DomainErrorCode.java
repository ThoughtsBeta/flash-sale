package com.actionworks.flashsale.domain.exception;


import com.alibaba.cola.dto.ErrorCodeI;

public enum DomainErrorCode implements ErrorCodeI {

    // 通用错误码
    PARAMS_INVALID("PARAMS_INVALID", "参数错误"),
    // 活动相关错误码
    ONLINE_FLASH_ACTIVITY_PARAMS_INVALID("ONLINE_FLASH_ACTIVITY_PARAMS_INVALID", "待上线的活动参数无效"),
    FLASH_ACTIVITY_DOES_NOT_EXIST("FLASH_ACTIVITY_DOES_NOT_EXIST", "活动不存在"),
    FLASH_ACTIVITY_ALREADY_OFFLINE("FLASH_ACTIVITY_ALREADY_OFFLINE", "活动已下线"),
    FLASH_ACTIVITY_NOT_ONLINE("FLASH_ACTIVITY_NOT_ONLINE", "活动尚未上线"),
    ACTIVITY_NOT_IN_PROGRESS("ACTIVITY_NOT_IN_PROGRESS", "当前不是活动时段"),
    // 商品相关错误码
    ONLINE_FLASH_ITEM_PARAMS_INVALID("ONLINE_FLASH_ITEM_PARAMS_INVALID", "待上线的秒杀品参数无效"),
    FLASH_ITEM_DOES_NOT_EXIST("FLASH_ITEM_DOES_NOT_EXIST", "秒杀品不存在"),
    FLASH_ITEM_NOT_ONLINE("FLASH_ITEM_NOT_ONLINE", "秒杀品尚未上线"),
    ITEM_NOT_IN_PROGRESS("ITEM_NOT_IN_PROGRESS", "当前不是秒杀时段"),
    FLASH_ITEM_ALREADY_OFFLINE("FLASH_ITEM_ALREADY_OFFLINE", "秒杀品已下线"),

    // 分桶相关错误码

    PRIMARY_BUCKET_IS_MISSING("PRIMARY_BUCKET_IS_MISSING", "主桶缺失"),
    MULTI_PRIMARY_BUCKETS_FOUND_BUT_EXPECT_ONE("MULTI_PRIMARY_BUCKETS_FOUND_BUT_EXPECT_ONE", "发现多个主桶但只需要一个"),
    TOTAL_STOCKS_AMOUNT_INVALID("TOTAL_STOCKS_AMOUNT_INVALID", "库存总数错误"),
    AVAILABLE_STOCKS_AMOUNT_NOT_EQUALS_TO_TOTAL_STOCKS_AMOUNT("AVAILABLE_STOCKS_AMOUNT_NOT_EQUALS_TO_TOTAL_STOCKS_AMOUNT", "子桶可用库存与库存总数不匹配"),
    AVAILABLE_STOCKS_AMOUNT_INVALID("AVAILABLE_STOCKS_AMOUNT_INVALID", "子桶可用库存数量错误"),
    STOCK_BUCKET_ITEM_INVALID("STOCK_BUCKET_ITEM_INVALID", "秒杀品ID设置错误");


    private final String errCode;
    private final String errDesc;

    DomainErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    @Override
    public String getErrCode() {
        return errCode;
    }

    @Override
    public String getErrDesc() {
        return errDesc;
    }
}