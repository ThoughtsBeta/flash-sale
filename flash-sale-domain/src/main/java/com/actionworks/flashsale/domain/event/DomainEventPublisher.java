package com.actionworks.flashsale.domain.event;

import com.alibaba.cola.event.DomainEventI;


public interface DomainEventPublisher {
    public void publish(DomainEventI domainEvent);
}