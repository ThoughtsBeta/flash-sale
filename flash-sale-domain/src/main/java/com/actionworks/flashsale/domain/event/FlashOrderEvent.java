package com.actionworks.flashsale.domain.event;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

@Data
public class FlashOrderEvent implements DomainEventI {
    private FlashOrderEventType eventType;
    private Long orderId;
}
