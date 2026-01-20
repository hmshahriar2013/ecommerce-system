package com.konasl.inventory.infrastructure.config;

import com.konasl.common.domain.DomainEvent;
import com.konasl.common.messaging.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Domain event publisher implementation using Spring's
 * ApplicationEventPublisher.
 */
@Component
public class DomainEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event, String routingKey) {
        applicationEventPublisher.publishEvent(event);
    }
}
