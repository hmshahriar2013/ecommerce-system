package com.konasl.common.messaging;

import com.konasl.common.domain.DomainEvent;

/**
 * Consumer for handling domain events from the message broker.
 * 
 * MESSAGING PATTERN:
 * - Subscribes to RabbitMQ queues
 * - Processes events from other bounded contexts
 * - Implements event-driven architecture
 * 
 * HEXAGONAL ARCHITECTURE:
 * - This is implemented in the inbound adapter layer
 * - Delegates to application layer use cases/handlers
 * - Adapter translates message to domain event
 * 
 * IDEMPOTENCY:
 * - Consumers MUST be idempotent (handle duplicate events)
 * - Use event ID to detect and skip duplicates
 * - Store processed event IDs or use natural idempotency
 * 
 * ERROR HANDLING:
 * - Failed events go to retry queue
 * - After max retries, events go to dead letter queue
 * - Manual intervention required for DLQ events
 */
public interface EventConsumer {

    /**
     * Handles a domain event received from the message broker.
     * 
     * @param event The domain event to handle
     */
    void handleEvent(DomainEvent event);

    /**
     * Returns the event types this consumer can handle.
     * Used for message routing and subscription setup.
     */
    Class<? extends DomainEvent>[] getSupportedEventTypes();
}
