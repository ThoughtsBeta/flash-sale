package com.actionworks.flashsale.app.service;

import com.actionworks.flashsale.app.model.command.FlashItemPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashItemDTO;
import com.actionworks.flashsale.app.model.query.FlashItemsQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSingleResult;


public interface FlashItemAppService {
    AppResult publishFlashItem(String token, Long activityId, FlashItemPublishCommand flashItemPublishCommand);

    AppResult onlineFlashItem(String token, Long activityId, Long itemId);

    AppResult offlineFlashItem(String token, Long activityId, Long itemId);

    AppMultiResult<FlashItemDTO> getFlashItems(String token, Long activityId, FlashItemsQuery flashItemsQuery);


    AppSingleResult<FlashItemDTO> getFlashItem(String token, Long activityId, Long itemId, Long version);
}
