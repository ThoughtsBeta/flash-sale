package com.actionworks.flashsale.domain.event;

import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

@Data
public class FlashActivityEvent implements DomainEventI {
    private FlashActivityEventType eventType;
    private FlashActivity flashActivity;

    public Long getId() {
        if (flashActivity == null) {
            return null;
        }
        return flashActivity.getId();
    }
}
