package com.actionworks.flashsale.controller.model.builder;

import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;

public class ResponseBuilder {
    public static Response with(AppResult appResult) {
        if (appResult == null) {
            return new Response();
        }
        Response response = new Response();
        response.setSuccess(appResult.isSuccess());
        response.setErrCode(appResult.getErrCode());
        response.setErrMessage(appResult.getErrMessage());
        return response;
    }

    public static <T> MultiResponse<T> withMulti(AppMultiResult appResult) {
        if (appResult == null) {
            return new MultiResponse<>();
        }
        MultiResponse multiResponse = new MultiResponse();
        multiResponse.setSuccess(appResult.isSuccess());
        multiResponse.setErrCode(appResult.getErrCode());
        multiResponse.setErrMessage(appResult.getErrMessage());
        multiResponse.setData(appResult.getData());
        return multiResponse;
    }

    public static <T> SingleResponse<T> withSingle(AppSimpleResult appResult) {
        if (appResult == null) {
            return new SingleResponse<>();
        }
        SingleResponse singleResponse = new SingleResponse();
        singleResponse.setSuccess(appResult.isSuccess());
        singleResponse.setErrCode(appResult.getCode());
        singleResponse.setErrMessage(appResult.getMessage());
        singleResponse.setData(appResult.getData());
        return singleResponse;
    }
}
