package com.actionworks.flashsale.app.model.builder;

import com.actionworks.flashsale.app.model.command.FlashItemPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashItemDTO;
import com.actionworks.flashsale.app.model.query.FlashItemsQuery;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import org.springframework.beans.BeanUtils;

public class FlashItemAppBuilder {
    public static FlashItem toDomain(FlashItemPublishCommand flashItemPublishCommand) {
        FlashItem flashItem = new FlashItem();
        BeanUtils.copyProperties(flashItemPublishCommand, flashItem);
        return flashItem;
    }

    public static PagesQueryCondition toFlashItemsQuery(FlashItemsQuery flashItemsQuery) {
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        BeanUtils.copyProperties(flashItemsQuery, pagesQueryCondition);
        return pagesQueryCondition;
    }

    public static FlashItemDTO toFlashItemDTO(FlashItem flashItem) {
        FlashItemDTO flashItemDto = new FlashItemDTO();
        BeanUtils.copyProperties(flashItem, flashItemDto);
        return flashItemDto;
    }
}
