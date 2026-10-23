package com.taller2.microservices.events.publisher;

import com.taller2.microservices.events.model.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
