package com.actionworks.flashsale.app.model.result;

import com.actionworks.flashsale.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.actionworks.flashsale.app.exception.AppErrorCode.TRY_LATER;

@Data
@Accessors(chain = true)
public class AppSingleResult<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> AppSingleResult<T> ok(T data) {
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
        appSingleResult.setCode(TRY_LATER.getErrCode());
        appSingleResult.setMessage(TRY_LATER.getErrDesc());
        return appSingleResult;
    }

    public static <T> AppSingleResult<T> failed(String errCode, String errDesc) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setCode(errCode);
        appSingleResult.setMessage(errDesc);
        return appSingleResult;
    }

    public static <T> AppSingleResult<T> failed(String errCode, String errDesc, T data) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setData(data);
        appSingleResult.setCode(errCode);
        appSingleResult.setMessage(errDesc);
        return appSingleResult;
    }

    public static <T> AppSingleResult<T> failed(AppErrorCode appErrorCode) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setCode(appErrorCode.getErrCode());
        appSingleResult.setMessage(appErrorCode.getErrDesc());
        return appSingleResult;
    }
}
