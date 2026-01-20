package com.konasl.common.outbox;

import com.konasl.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Represents an event stored in the outbox for reliable message delivery.
 * 
 * OUTBOX PATTERN:
 * - Events are persisted atomically with business data
 * - Background process publishes events from outbox to message broker
 * - Ensures at-least-once delivery guarantee
 * - Prevents message loss during failures
 * 
 * WORKFLOW:
 * 1. Business transaction saves aggregate + events to outbox
 * 2. Transaction commits (both data and outbox in same transaction)
 * 3. Dispatcher polls outbox and publishes events
 * 4. Events are marked as dispatched after successful publish
 * 
 * HEXAGONAL ARCHITECTURE:
 * - Outbox is in the adapter layer
 * - Domain events are produced by domain layer
 * - Infrastructure layer implements outbox persistence
 */
public class OutboxEvent {

    private String id;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private String eventPayload;
    private Instant occurredAt;
    private Instant createdAt;
    private Instant dispatchedAt;
    private OutboxStatus status;
    private int retryCount;
    private String errorMessage;

    public OutboxEvent() {
        this.createdAt = Instant.now();
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    /**
     * Creates an outbox event from a domain event.
     */
    public static OutboxEvent fromDomainEvent(String id, DomainEvent event, String eventPayload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(id);
        outboxEvent.setAggregateId(event.getAggregateId());
        outboxEvent.setAggregateType(event.getAggregateType());
        outboxEvent.setEventType(event.getClass().getSimpleName());
        outboxEvent.setEventPayload(eventPayload);
        outboxEvent.setOccurredAt(event.getOccurredAt());
        return outboxEvent;
    }

    /**
     * Marks the event as dispatched.
     */
    public void markAsDispatched() {
        this.status = OutboxStatus.DISPATCHED;
        this.dispatchedAt = Instant.now();
    }

    /**
     * Marks the event as failed with error message.
     */
    public void markAsFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * Resets the event for retry.
     */
    public void resetForRetry() {
        this.status = OutboxStatus.PENDING;
        this.errorMessage = null;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventPayload() {
        return eventPayload;
    }

    public void setEventPayload(String eventPayload) {
        this.eventPayload = eventPayload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(Instant dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
