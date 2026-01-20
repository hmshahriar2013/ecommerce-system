package com.konasl.catalog.infrastructure.config;

import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Helper class to publish domain events for read model projection.
 * 
 * MVP: Uses Spring's ApplicationEventPublisher
 * TODO: Replace with RabbitMQ EventPublisher
 */
public class DomainEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(DomainEvent event) {
        logger.debug("Publishing domain event: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
