package com.actionworks.flashsale.persistence.convertor;

import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.persistence.model.FlashItemDO;
import org.springframework.beans.BeanUtils;

public class FlashItemBuilder {

    public static FlashItemDO toDataObjectForCreate(FlashItem flashItem) {
        FlashItemDO flashItemDO = new FlashItemDO();
        BeanUtils.copyProperties(flashItem, flashItemDO);
        return flashItemDO;
    }

    public static FlashItem toDomainObject(FlashItemDO flashItemDO) {
        FlashItem flashItem = new FlashItem();
        BeanUtils.copyProperties(flashItemDO, flashItem);
        return flashItem;
    }
}
