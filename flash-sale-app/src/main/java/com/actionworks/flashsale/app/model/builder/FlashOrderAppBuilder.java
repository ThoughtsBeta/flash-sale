package com.actionworks.flashsale.app.model.builder;

import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.app.model.query.FlashOrdersQuery;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import org.springframework.beans.BeanUtils;

public class FlashOrderAppBuilder {
    public static FlashOrder toDomain(FlashPlaceOrderCommand flashPlaceOrderCommand) {
        if (flashPlaceOrderCommand == null) {
            return null;
        }
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(flashPlaceOrderCommand, flashOrder);
        return flashOrder;
    }

    public static FlashOrder toDomain(PlaceOrderTask PlaceOrderTask) {
        if (PlaceOrderTask == null) {
            return null;
        }
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(PlaceOrderTask, flashOrder);
        return flashOrder;
    }

    public static PagesQueryCondition toFlashOrdersQuery(FlashOrdersQuery flashOrdersQuery) {
        if (flashOrdersQuery == null) {
            return new PagesQueryCondition();
        }
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        BeanUtils.copyProperties(flashOrdersQuery, pagesQueryCondition);
        return pagesQueryCondition;
    }

    public static FlashOrderDTO toFlashOrderDTO(FlashOrder flashOrder) {
        if (flashOrder == null) {
            return null;
        }
        FlashOrderDTO flashOrderDTO = new FlashOrderDTO();
        BeanUtils.copyProperties(flashOrder, flashOrderDTO);
        return flashOrderDTO;
    }
}
