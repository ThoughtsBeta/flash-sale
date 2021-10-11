package com.actionworks.flashsale.persistence.convertor;

import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.persistence.model.FlashOrderDO;
import org.springframework.beans.BeanUtils;

public class FlashOrderBuilder {

    public static FlashOrderDO toDataObjectForCreate(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = new FlashOrderDO();
        BeanUtils.copyProperties(flashOrder, flashOrderDO);
        return flashOrderDO;
    }

    public static FlashOrder toDomainObject(FlashOrderDO flashOrderDO) {
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(flashOrderDO, flashOrder);
        return flashOrder;
    }
}
