package com.actionworks.flashsale.app.service;

import com.actionworks.flashsale.app.model.command.FlashActivityPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashActivityDTO;
import com.actionworks.flashsale.app.model.query.FlashActivitiesQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSingleResult;

public interface FlashActivityAppService {
    AppMultiResult<FlashActivityDTO> getFlashActivities(String token, FlashActivitiesQuery flashActivitiesQuery);

    AppSingleResult<FlashActivityDTO> getFlashActivity(String token, Long activityId, Long version);

    AppResult publishFlashActivity(String token, FlashActivityPublishCommand flashActivityPublishCommand);

    AppResult modifyFlashActivity(String token, Long activityId, FlashActivityPublishCommand flashActivityPublishCommand);

    AppResult onlineFlashActivity(String token, Long activityId);

    AppResult offlineFlashActivity(String token, Long activityId);
}
