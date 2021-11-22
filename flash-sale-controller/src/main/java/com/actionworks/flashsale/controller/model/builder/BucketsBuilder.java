package com.actionworks.flashsale.controller.model.builder;

import com.actionworks.flashsale.app.model.command.BucketsArrangementCommand;
import com.actionworks.flashsale.controller.model.request.BucketsArrangementRequest;
import org.springframework.beans.BeanUtils;

public class BucketsBuilder {
    public static BucketsArrangementCommand toCommand(BucketsArrangementRequest bucketsArrangementRequest) {
        BucketsArrangementCommand bucketsArrangementCommand = new BucketsArrangementCommand();
        BeanUtils.copyProperties(bucketsArrangementRequest, bucketsArrangementCommand);
        return bucketsArrangementCommand;
    }
}
