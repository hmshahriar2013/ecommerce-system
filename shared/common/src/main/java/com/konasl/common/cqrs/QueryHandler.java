package com.konasl.common.cqrs;

/**
 * Handler for executing queries.
 * Each query type should have exactly one handler.
 * 
 * CQRS PATTERN:
 * - Query handlers read from read models/projections
 * - Query handlers should NOT modify state
 * - Query handlers can use different databases/storage than write side
 * - Query handlers should be fast and optimized for reads
 * 
 * HEXAGONAL ARCHITECTURE:
 * - Query handlers belong to the application layer
 * - Query handlers depend on query repositories (read models)
 * - Query handlers should NOT have framework dependencies
 * 
 * @param <Q> The query type this handler processes
 * @param <R> The result type returned by this query
 */
public interface QueryHandler<Q extends Query<R>, R> {

    /**
     * Executes the query and returns the result.
     * 
     * @param query The query to execute
     * @return The query result
     * @throws IllegalArgumentException if query validation fails
     */
    R handle(Q query);

    /**
     * Returns the query type this handler can process.
     * Used for query routing and registration.
     */
    Class<Q> getQueryType();
}
