package com.konasl.common.eventsourcing;

/**
 * Exception thrown when concurrent modifications are detected in event
 * sourcing.
 * Occurs when two processes try to append events to the same aggregate
 * simultaneously.
 * 
 * OPTIMISTIC LOCKING:
 * - Each aggregate has a version number
 * - Version is incremented with each event
 * - Save operation checks expected version matches stored version
 * - If versions don't match, this exception is thrown
 * 
 * RECOVERY STRATEGIES:
 * - Retry the operation (reload aggregate and re-execute command)
 * - Inform the user of the conflict
 * - Use conflict resolution strategy (if applicable)
 */
public class ConcurrencyException extends RuntimeException {

    private final String aggregateId;
    private final long expectedVersion;
    private final long actualVersion;

    public ConcurrencyException(String aggregateId, long expectedVersion, long actualVersion) {
        super(String.format(
                "Concurrency conflict for aggregate %s: expected version %d, but actual version is %d",
                aggregateId, expectedVersion, actualVersion));
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public long getExpectedVersion() {
        return expectedVersion;
    }

    public long getActualVersion() {
        return actualVersion;
    }
}
