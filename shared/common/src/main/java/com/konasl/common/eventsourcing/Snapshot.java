package com.konasl.common.eventsourcing;

import com.konasl.common.domain.AggregateRoot;

import java.time.Instant;

/**
 * Represents a snapshot of an aggregate's state at a specific point in time.
 * Snapshots are performance optimizations to avoid replaying all events.
 * 
 * SNAPSHOT PATTERN:
 * - Snapshots capture aggregate state at a specific version
 * - Snapshots are optional optimizations, not required
 * - Aggregate can be rebuilt from snapshot + subsequent events
 * - Snapshots reduce event replay overhead for long-lived aggregates
 * 
 * USAGE:
 * - Create snapshot after N events (e.g., every 100 events)
 * - Load snapshot first, then apply events since snapshot
 * - Snapshots are immutable once created
 * 
 * @param <T> The aggregate type
 */
public class Snapshot<T extends AggregateRoot> {

    private final String aggregateId;
    private final String aggregateType;
    private final long version;
    private final T aggregateState;
    private final Instant createdAt;

    public Snapshot(String aggregateId, String aggregateType, long version, T aggregateState) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.aggregateState = aggregateState;
        this.createdAt = Instant.now();
    }

    /**
     * Returns the aggregate ID this snapshot belongs to.
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Returns the type of aggregate.
     */
    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * Returns the version of the aggregate when this snapshot was taken.
     */
    public long getVersion() {
        return version;
    }

    /**
     * Returns the aggregate state captured in this snapshot.
     */
    public T getAggregateState() {
        return aggregateState;
    }

    /**
     * Returns when this snapshot was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
