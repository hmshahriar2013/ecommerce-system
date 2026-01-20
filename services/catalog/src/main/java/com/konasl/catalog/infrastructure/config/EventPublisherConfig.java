package com.konasl.catalog.infrastructure.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for event publishing.
 * 
 * MVP: Using Spring's ApplicationEventPublisher for in-process events
 * TODO: Replace with RabbitMQ for distributed architecture
 */
@Configuration
public class EventPublisherConfig {

    @Bean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DomainEventPublisher(applicationEventPublisher);
    }
}
