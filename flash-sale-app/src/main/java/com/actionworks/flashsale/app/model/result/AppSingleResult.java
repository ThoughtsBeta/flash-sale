package com.actionworks.flashsale.app.model.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.actionworks.flashsale.app.exception.AppErrorCode.TRY_LATER;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppSingleResult<T> extends AppResult {
    private boolean success;
    private String message;
    private T data;

    public static <T> AppSingleResult<T> of(T data) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(true);
        appSingleResult.setData(data);
        return appSingleResult;
    }

    /**
     * 数据更新中，稍后再试
     * 客户端对此类错误不做页面刷新等其他操作
     */
    public static <T> AppSingleResult<T> tryLater() {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setErrCode(TRY_LATER.getErrCode());
        appSingleResult.setErrMessage(TRY_LATER.getErrDesc());
        return appSingleResult;
    }
}
