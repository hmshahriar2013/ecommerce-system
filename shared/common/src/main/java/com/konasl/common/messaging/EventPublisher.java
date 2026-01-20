package com.konasl.common.messaging;

import com.konasl.common.domain.DomainEvent;

/**
 * Publisher for publishing domain events to the message broker.
 * 
 * MESSAGING PATTERN:
 * - Publishes events to RabbitMQ exchanges
 * - Supports routing keys for topic-based routing
 * - Enables asynchronous communication between bounded contexts
 * 
 * HEXAGONAL ARCHITECTURE:
 * - This is a port (interface) in the application layer
 * - Implementation is an adapter (RabbitMQ adapter)
 * - Domain layer is unaware of messaging infrastructure
 * 
 * USAGE:
 * - Called by OutboxDispatcher to publish events from outbox
 * - Can also be used directly for fire-and-forget scenarios
 * - Consumers in other bounded contexts subscribe to events
 */
public interface EventPublisher {

    /**
     * Publishes a domain event to the message broker.
     * 
     * @param event      The domain event to publish
     * @param routingKey Optional routing key for topic exchange
     */
    void publish(DomainEvent event, String routingKey);

    /**
     * Publishes a domain event to the default routing key.
     * Routing key is derived from aggregate type and event type.
     * 
     * @param event The domain event to publish
     */
    default void publish(DomainEvent event) {
        String routingKey = event.getAggregateType() + "." + event.getClass().getSimpleName();
        publish(event, routingKey);
    }
}
