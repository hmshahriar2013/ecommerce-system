package com.konasl.pricing.domain;

import com.konasl.common.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event emitted when price is set for a product.
 */
public record PriceSetEvent(
        String eventId,
        String aggregateId,
        String productId,
        BigDecimal amount,
        String currency,
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
        return "Price";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }
}
