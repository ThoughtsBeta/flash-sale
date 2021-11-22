package com.actionworks.flashsale.domain.event;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

@Data
public class StockBucketEvent implements DomainEventI {
    private StockBucketEventType eventType;
    private Long itemId;
}
