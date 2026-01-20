package com.konasl.common.eventsourcing;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;

import java.util.List;

/**
 * Repository for persisting and retrieving event-sourced aggregates.
 * 
 * EVENT SOURCING PATTERN:
 * - Events are the source of truth
 * - Aggregates are reconstituted from events
 * - New events are appended to the event stream
 * - Events are immutable and never deleted
 * 
 * CONCURRENCY:
 * - Uses optimistic locking via version numbers
 * - Detects concurrent modifications
 * - Throws ConcurrencyException on version conflicts
 * 
 * @param <T> The aggregate type
 */
public interface EventStore<T extends AggregateRoot> {

    /**
     * Saves the uncommitted events from an aggregate to the event store.
     * 
     * @param aggregate The aggregate with uncommitted events
     * @throws ConcurrencyException if the aggregate version conflicts with stored
     *                              version
     */
    void save(T aggregate);

    /**
     * Loads an aggregate by reconstituting it from its event history.
     * 
     * @param aggregateId The unique identifier of the aggregate
     * @return The reconstituted aggregate, or null if not found
     */
    T load(String aggregateId);

    /**
     * Checks if an aggregate exists in the event store.
     * 
     * @param aggregateId The unique identifier of the aggregate
     * @return true if the aggregate exists, false otherwise
     */
    boolean exists(String aggregateId);

    /**
     * Retrieves all events for a specific aggregate.
     * 
     * @param aggregateId The unique identifier of the aggregate
     * @return List of events in chronological order
     */
    List<DomainEvent> getEvents(String aggregateId);

    /**
     * Retrieves events for an aggregate starting from a specific version.
     * Useful for incremental updates and snapshots.
     * 
     * @param aggregateId The unique identifier of the aggregate
     * @param fromVersion The version to start from (inclusive)
     * @return List of events from the specified version
     */
    List<DomainEvent> getEventsFromVersion(String aggregateId, long fromVersion);
}
