package com.actionworks.flashsale.app.exception;

import com.alibaba.cola.dto.ErrorCodeI;

public enum AppErrorCode implements ErrorCodeI {

    TRY_LATER("TRY_LATER", "稍后再试"),
    OPERATE_TOO_QUICKILY_ERROR("OPERATE_TOO_QUICKILY_ERROR", "操作频繁，稍后再试"),
    UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误"),

    ACTIVITY_NOT_FOUND("ACTIVITY_NOT_FOUND", "活动不存在"),
    ACTIVITY_NOT_ONLINE("ACTIVITY_NOT_ONLINE", "活动未上线"),
    ACTIVITY_NOT_IN_PROGRESS("ACTIVITY_NOT_IN_PROGRESS", "当前不是活动时段"),

    ITEM_NOT_FOUND("ITEM_NOT_FOUND", "秒杀品不存在"),
    ITEM_NOT_ONLINE("ITEM_NOT_ONLINE", "秒杀品未上线"),
    ITEM_NOT_IN_PROGRESS("ITEM_NOT_IN_PROGRESS", "当前不是秒杀时段"),

    STOCK_PRE_DECREASE_FAILED("STOCK_PRE_DECREASE_FAILED", "库存扣减失败"),

    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "订单不存在"),
    ORDER_CANCEL_FAILED("ORDER_CANCEL_FAILED", "订单取消失败"),
    PLACE_ORDER_FAILED("PLACE_ORDER_FAILED", "下单失败");

    private final String errCode;
    private final String errDesc;

    private AppErrorCode(String errCode, String errDesc) {
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
