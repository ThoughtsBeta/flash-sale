package com.actionworks.flashsale.app.service.activity;

import com.actionworks.flashsale.app.model.command.FlashActivityPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashActivityDTO;
import com.actionworks.flashsale.app.model.query.FlashActivitiesQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;

public interface FlashActivityAppService {
    AppMultiResult<FlashActivityDTO> getFlashActivities(Long userId, FlashActivitiesQuery flashActivitiesQuery);

    AppSimpleResult<FlashActivityDTO> getFlashActivity(Long userId, Long activityId, Long version);

    AppResult publishFlashActivity(Long userId, FlashActivityPublishCommand flashActivityPublishCommand);

    AppResult modifyFlashActivity(Long userId, Long activityId, FlashActivityPublishCommand flashActivityPublishCommand);

    AppResult onlineFlashActivity(Long userId, Long activityId);

    AppResult offlineFlashActivity(Long userId, Long activityId);

    boolean isAllowPlaceOrderOrNot(Long activityId);
}
