package com.actionworks.flashsale.domain.event;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

@Data
public class FlashActivityOfflineEvent implements DomainEventI {
    private Long id;
}
