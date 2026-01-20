package com.konasl.common.outbox;

/**
 * Dispatcher responsible for publishing outbox events to the message broker.
 * 
 * OUTBOX PATTERN:
 * - Polls outbox repository for pending events
 * - Publishes events to message broker (RabbitMQ)
 * - Updates event status after successful/failed publish
 * - Implements retry logic with exponential backoff
 * 
 * RELIABILITY:
 * - At-least-once delivery guarantee
 * - Handles transient failures with retry
 * - Dead letter queue for permanently failed events
 * - Idempotency handling on consumer side required
 * 
 * IMPLEMENTATION:
 * - Should run as scheduled background task
 * - Implements @Scheduled in Spring Boot infrastructure layer
 * - Uses EventPublisher to send messages
 */
public interface OutboxDispatcher {

    /**
     * Dispatches pending events from the outbox to the message broker.
     * Should be called periodically (e.g., every 5 seconds).
     */
    void dispatchPendingEvents();

    /**
     * Retries failed events that are eligible for retry.
     * Implements exponential backoff strategy.
     */
    void retryFailedEvents();
}
