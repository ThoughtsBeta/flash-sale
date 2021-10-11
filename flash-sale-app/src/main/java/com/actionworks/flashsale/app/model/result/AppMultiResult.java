package com.actionworks.flashsale.app.model.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

import static com.actionworks.flashsale.app.exception.AppErrorCode.TRY_LATER;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppMultiResult<T> extends AppResult {
    private int total;
    private Collection<T> data;


    public static <T> AppMultiResult<T> of(Collection<T> data, int total) {
        AppMultiResult<T> tAppMultiResult = new AppMultiResult<>();
        tAppMultiResult.setSuccess(true);
        tAppMultiResult.setData(data);
        tAppMultiResult.setTotal(total);
        return tAppMultiResult;
    }

    /**
     * 数据更新中，稍后再试
     * 客户端对此类错误不做页面刷新等其他操作
     */
    public static <T> AppMultiResult<T> tryLater() {
        AppMultiResult<T> multiResult = new AppMultiResult<>();
        multiResult.setSuccess(false);
        multiResult.setErrCode(TRY_LATER.getErrCode());
        multiResult.setErrMessage(TRY_LATER.getErrDesc());
        return multiResult;
    }

    public static <T> AppMultiResult<T> empty() {
        AppMultiResult<T> multiResult = new AppMultiResult<>();
        multiResult.setSuccess(false);
        return multiResult;
    }
}
