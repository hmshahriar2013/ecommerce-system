package com.konasl.common.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

/**
 * Base interface for all domain events in the system.
 * Domain events represent facts that have happened in the domain.
 * 
 * EVENT SOURCING PATTERN:
 * - Events are immutable facts
 * - Events represent state changes
 * - Events are the source of truth for aggregate state
 * - Events must be versioned for schema evolution
 * 
 * DDD PATTERN:
 * - Events are named in past tense (OrderCreated, PaymentProcessed)
 * - Events belong to a specific aggregate
 * - Events should contain all data needed to rebuild state
 * - Events are published to other bounded contexts
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public interface DomainEvent {

    /**
     * Unique identifier for this event instance.
     * Used for idempotency and deduplication.
     */
    String getEventId();

    /**
     * ID of the aggregate that emitted this event.
     */
    String getAggregateId();

    /**
     * Type of the aggregate (e.g., "Order", "Customer").
     * Used for event routing and aggregate reconstruction.
     */
    String getAggregateType();

    /**
     * Version of the event schema.
     * MUST be incremented when event structure changes.
     * Format: "1.0", "2.0", etc.
     */
    String getEventVersion();

    /**
     * Timestamp when the event occurred.
     * This is the business timestamp, not the technical persistence timestamp.
     */
    Instant getOccurredAt();

    /**
     * ID of the user/system that caused this event.
     * Used for auditing and tracing.
     */
    String getCausedBy();

    /**
     * Optional correlation ID for tracking related events across aggregates.
     * Used for distributed tracing and saga orchestration.
     */
    default String getCorrelationId() {
        return null;
    }
}
