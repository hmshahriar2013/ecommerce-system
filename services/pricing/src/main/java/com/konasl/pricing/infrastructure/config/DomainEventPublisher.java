package com.konasl.pricing.infrastructure.config;

import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher for domain events using Spring's ApplicationEventPublisher.
 */
@Component
public class DomainEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish(DomainEvent event) {
        logger.debug("Publishing event: {}", event.getClass().getSimpleName());
        eventPublisher.publishEvent(event);
    }
}
