package com.actionworks.flashsale.domain.event;

import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

@Data
public class FlashItemEvent implements DomainEventI {
    private FlashItemEventType eventType;
    private FlashItem flashItem;

    public Long getId() {
        if (flashItem == null) {
            return null;
        }
        return flashItem.getId();
    }

    public Long getFlashActivityId() {
        if (flashItem == null) {
            return null;
        }
        return flashItem.getActivityId();
    }
}
