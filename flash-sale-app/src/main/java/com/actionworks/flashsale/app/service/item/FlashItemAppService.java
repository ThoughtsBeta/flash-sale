package com.actionworks.flashsale.app.service.item;

import com.actionworks.flashsale.app.model.command.FlashItemPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashItemDTO;
import com.actionworks.flashsale.app.model.query.FlashItemsQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;


public interface FlashItemAppService {
    AppResult publishFlashItem(String token, Long activityId, FlashItemPublishCommand flashItemPublishCommand);

    AppResult onlineFlashItem(String token, Long activityId, Long itemId);

    AppResult offlineFlashItem(String token, Long activityId, Long itemId);

    AppMultiResult<FlashItemDTO> getFlashItems(String token, Long activityId, FlashItemsQuery flashItemsQuery);


    AppSimpleResult<FlashItemDTO> getFlashItem(String token, Long activityId, Long itemId, Long version);

    AppSimpleResult<FlashItemDTO> getFlashItem(Long itemId);

    /**
     * 检查活动当前是否允许下单，当条件不满足时将抛出异常
     *
     * @param itemId 秒杀品ID
     */
    boolean isAllowPlaceOrderOrNot(Long itemId);
}
