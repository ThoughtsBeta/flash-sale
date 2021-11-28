package com.actionworks.flashsale.app.model.command;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
public class FlashActivityPublishCommand {
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

    public boolean validate() {
        return StringUtils.isNotEmpty(activityName)
                && startTime != null
                && endTime != null
                && startTime.before(endTime);
    }
}
