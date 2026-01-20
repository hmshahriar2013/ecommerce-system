package com.konasl.catalog.domain;

import com.konasl.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event emitted when product details are updated.
 */
public record ProductUpdatedEvent(
        String eventId,
        String aggregateId,
        String name,
        String description,
        String category,
        String imageUrl,
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
    public String getAggregateType() {
        return "Product";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getCausedBy() {
        return causedBy;
    }
}
