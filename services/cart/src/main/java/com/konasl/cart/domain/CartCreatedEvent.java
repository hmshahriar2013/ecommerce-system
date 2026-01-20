package com.konasl.cart.domain;

import com.konasl.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event emitted when a cart is created.
 */
public record CartCreatedEvent(
        String eventId,
        String aggregateId,
        String userId,
        Instant occurredAt,
        String causedBy) implements DomainEvent {

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getCausedBy() {
        return causedBy;
    }

    @Override
    public String getAggregateType() {
        return "Cart";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }
}
