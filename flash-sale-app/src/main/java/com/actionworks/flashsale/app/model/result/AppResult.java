package com.actionworks.flashsale.app.model.result;

import com.actionworks.flashsale.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AppResult {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String errCode;
    private String errMessage;

    public static AppResult buildFailure(String errCode, String errMessage) {
        AppResult appResult = new AppResult();
        appResult.setSuccess(false);
        appResult.setErrCode(errCode);
        appResult.setErrMessage(errMessage);
        return appResult;
    }

    public static AppResult buildSuccess() {
        AppResult appResult = new AppResult();
        appResult.setSuccess(true);
        return appResult;
    }

    public static AppResult buildFailure(AppErrorCode appErrorCode) {
        AppResult appResult = new AppResult();
        appResult.setSuccess(false);
        appResult.setErrCode(appErrorCode.getErrCode());
        appResult.setErrMessage(appErrorCode.getErrDesc());
        return appResult;
    }
}
