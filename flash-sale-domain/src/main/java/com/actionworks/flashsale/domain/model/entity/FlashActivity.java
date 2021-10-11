package com.actionworks.flashsale.domain.model.entity;

import com.actionworks.flashsale.domain.model.enums.FlashActivityStatus;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlashActivity implements Serializable {
    /**
     * 活动ID
     */
    private Long Id;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 活动开始时间
     */
    private Date startTime;
    /**
     * 活动结束时间
     */
    private Date endTime;
    /**
     * 活动状态
     */
    private Integer status;
    /**
     * 活动描述
     */
    private String activityDesc;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public boolean validateParamsForCreateOrUpdate() {
        if (StringUtils.isEmpty(activityName)
                || startTime == null
                || endTime == null || endTime.before(startTime) || endTime.before(new Date())) {
            return false;
        }
        return true;
    }

    public boolean isOnline() {
        return FlashActivityStatus.isOnline(status);
    }

    public boolean isInProgress() {
        Date now = new Date();
        return startTime.before(now) && endTime.after(now);
    }
}
