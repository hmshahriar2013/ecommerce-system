package com.konasl.common.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all event-sourced aggregates.
 * Aggregates are consistency boundaries that enforce business invariants.
 * 
 * EVENT SOURCING PATTERN:
 * - Aggregate state is rebuilt from events
 * - All state changes must produce events
 * - Events are applied to mutate state
 * - No direct state mutation allowed
 * 
 * DDD PATTERN:
 * - Aggregates are the main building blocks of the domain model
 * - Aggregates protect business invariants
 * - Aggregates have a unique identity (aggregate root)
 * - Only aggregate roots can be referenced from outside
 * 
 * USAGE:
 * 1. Extend this class in your aggregate root
 * 2. Implement event application logic (apply methods)
 * 3. Emit events for every state change (raiseEvent)
 * 4. Rebuild state from events (loadFromHistory)
 */
public abstract class AggregateRoot {

    private String aggregateId;
    private long version;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    protected AggregateRoot() {
        this.version = 0;
    }

    protected AggregateRoot(String aggregateId) {
        this.aggregateId = aggregateId;
        this.version = 0;
    }

    /**
     * Returns the unique identifier of this aggregate.
     */
    public String getAggregateId() {
        return aggregateId;
    }

    protected void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    /**
     * Returns the current version of this aggregate.
     * Version is incremented with each event.
     * Used for optimistic concurrency control.
     */
    public long getVersion() {
        return version;
    }

    /**
     * Returns the list of uncommitted events.
     * These events have been raised but not yet persisted.
     */
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    /**
     * Marks all uncommitted events as committed.
     * Called after events are successfully persisted.
     */
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }

    /**
     * Raises a new domain event.
     * The event is added to uncommitted events and applied to update state.
     * 
     * @param event The domain event to raise
     */
    protected void raiseEvent(DomainEvent event) {
        applyEvent(event, true);
    }

    /**
     * Loads aggregate state from historical events.
     * Used when reconstituting aggregate from event store.
     * 
     * @param history The list of historical events
     */
    public void loadFromHistory(List<DomainEvent> history) {
        for (DomainEvent event : history) {
            applyEvent(event, false);
        }
    }

    /**
     * Applies an event to the aggregate state.
     * 
     * @param event The event to apply
     * @param isNew Whether this is a new event (true) or historical event (false)
     */
    private void applyEvent(DomainEvent event, boolean isNew) {
        // Apply the event to update state
        apply(event);

        // Increment version
        version++;

        // Add to uncommitted events if new
        if (isNew) {
            uncommittedEvents.add(event);
        }
    }

    /**
     * Apply the event to update aggregate state.
     * Subclasses must implement this method to handle their specific events.
     * 
     * Pattern: Use instanceof or visitor pattern to dispatch to specific apply
     * methods
     * 
     * @param event The event to apply
     */
    protected abstract void apply(DomainEvent event);
}
