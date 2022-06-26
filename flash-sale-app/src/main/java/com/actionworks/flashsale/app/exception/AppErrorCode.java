package com.actionworks.flashsale.app.exception;

import com.alibaba.cola.dto.ErrorCodeI;

public enum AppErrorCode implements ErrorCodeI {

    /**
     * 一般性错误
     */
    INVALID_PARAMS("INVALID_PARAMS", "参数错误"),
    TRY_LATER("TRY_LATER", "稍后再试"),
    FREQUENTLY_ERROR("FREQUENTLY_ERROR", "操作频繁，稍后再试"),
    LOCK_FAILED_ERROR("LOCK_FAILED_ERROR", "变更中，稍后再试"),
    BUSINESS_ERROR("BUSINESS_ERROR", "未知错误"),

    /**
     * 活动相关错误
     */
    ACTIVITY_NOT_FOUND("ACTIVITY_NOT_FOUND", "活动不存在"),
    ACTIVITY_NOT_ONLINE("ACTIVITY_NOT_ONLINE", "活动未上线"),
    ACTIVITY_NOT_IN_PROGRESS("ACTIVITY_NOT_IN_PROGRESS", "当前不是活动时段"),

    /**
     * 秒杀品相关错误
     */
    ITEM_NOT_FOUND("ITEM_NOT_FOUND", "秒杀品不存在"),
    ITEM_NOT_ONLINE("ITEM_NOT_ONLINE", "秒杀品未上线"),
    ITEM_NOT_ON_SALE("ITEM_NOT_ON_SALE", "当前不是秒杀时段"),

    STOCK_PRE_DECREASE_FAILED("STOCK_PRE_DECREASE_FAILED", "库存扣减失败"),

    /**
     * 下单错误
     */
    GET_ITEM_FAILED("GET_ITEM_FAILED", "获取秒杀品失败"),
    ITEM_SOLD_OUT("ITEM_SOLD_OUT", "秒杀品已售罄"),
    REDUNDANT_SUBMIT("REDUNDANT_SUBMIT", "请勿重复下单"),
    ORDER_TOKENS_NOT_AVAILABLE("ORDER_TOKENS_NOT_AVAILABLE", "暂无可用库存"),
    ORDER_TASK_SUBMIT_FAILED("ORDER_TASK_SUBMIT_FAILED", "订单提交失败，请稍后再试"),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "订单不存在"),
    ORDER_TYPE_NOT_SUPPORT("ORDER_TYPE_NOT_SUPPORT", "下单类型不支持"),
    ORDER_CANCEL_FAILED("ORDER_CANCEL_FAILED", "订单取消失败"),
    PLACE_ORDER_FAILED("PLACE_ORDER_FAILED", "下单失败"),
    PLACE_ORDER_TASK_ID_INVALID("PLACE_ORDER_TASK_ID_INVALID", "下单任务编号错误"),

    ARRANGE_STOCK_BUCKETS_FAILED("ARRANGE_STOCK_BUCKETS_FAILED", "库存编排错误"),
    QUERY_STOCK_BUCKETS_FAILED("QUERY_STOCK_BUCKETS_FAILED", "获取库存分桶错误");

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
