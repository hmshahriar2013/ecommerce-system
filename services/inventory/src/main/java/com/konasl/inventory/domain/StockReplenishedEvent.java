package com.konasl.inventory.domain;

import com.konasl.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event emitted when stock quantity is replenished.
 */
public record StockReplenishedEvent(
        String eventId,
        String aggregateId,
        int quantityAdded,
        int newTotalQuantity,
        Instant occurredAt,
        String causedBy) implements DomainEvent {

    /**
     * Unique identifier for this event instance.
     * Used for idempotency and deduplication.
     */
    @Override
    public String getEventId() {
        return eventId;
    }

    /**
     * ID of the aggregate that emitted this event.
     */
    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Timestamp when the event occurred.
     * This is the business timestamp, not the technical persistence timestamp.
     */
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * ID of the user/system that caused this event.
     * Used for auditing and tracing.
     */
    @Override
    public String getCausedBy() {
        return causedBy;
    }

    @Override
    public String getAggregateType() {
        return "Stock";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }
}
