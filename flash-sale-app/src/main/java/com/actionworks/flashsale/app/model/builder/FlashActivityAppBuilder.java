package com.actionworks.flashsale.app.model.builder;

import com.actionworks.flashsale.app.model.command.FlashActivityPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashActivityDTO;
import com.actionworks.flashsale.app.model.query.FlashActivitiesQuery;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import org.springframework.beans.BeanUtils;

public class FlashActivityAppBuilder {
    public static FlashActivity toDomain(FlashActivityPublishCommand flashActivityPublishCommand) {
        if (flashActivityPublishCommand == null) {
            return null;
        }
        FlashActivity flashActivity = new FlashActivity();
        BeanUtils.copyProperties(flashActivityPublishCommand, flashActivity);
        return flashActivity;
    }

    public static PagesQueryCondition toFlashActivitiesQuery(FlashActivitiesQuery flashActivitiesQuery) {
        if (flashActivitiesQuery == null) {
            return new PagesQueryCondition();
        }
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        BeanUtils.copyProperties(flashActivitiesQuery, pagesQueryCondition);
        return pagesQueryCondition;
    }

    public static FlashActivityDTO toFlashActivityDTO(FlashActivity flashActivity) {
        if (flashActivity == null) {
            return null;
        }
        FlashActivityDTO flashActivityDTO = new FlashActivityDTO();
        BeanUtils.copyProperties(flashActivity, flashActivityDTO);
        return flashActivityDTO;
    }
}
