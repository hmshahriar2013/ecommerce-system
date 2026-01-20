package com.konasl.common.outbox;

import java.util.List;

/**
 * Repository for managing outbox events.
 * 
 * OUTBOX PATTERN:
 * - Provides persistence for outbox events
 * - Supports querying pending events for dispatch
 * - Enables atomic persistence with business data
 * 
 * IMPLEMENTATION:
 * - Must be implemented in the adapter layer
 * - Should use the same database as business data
 * - Must support transactional operations
 */
public interface OutboxRepository {

    /**
     * Saves an outbox event.
     * Must be called within the same transaction as business data persistence.
     */
    void save(OutboxEvent event);

    /**
     * Retrieves pending events that need to be dispatched.
     * 
     * @param limit Maximum number of events to retrieve
     * @return List of pending outbox events
     */
    List<OutboxEvent> findPendingEvents(int limit);

    /**
     * Updates an existing outbox event.
     */
    void update(OutboxEvent event);

    /**
     * Deletes dispatched events older than the specified timestamp.
     * Used for cleanup to prevent outbox table from growing indefinitely.
     */
    void deleteDispatchedEventsBefore(java.time.Instant timestamp);
}
