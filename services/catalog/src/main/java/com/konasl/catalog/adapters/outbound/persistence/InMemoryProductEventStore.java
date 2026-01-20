package com.konasl.catalog.adapters.outbound.persistence;

import com.konasl.catalog.application.port.ProductEventStore;
import com.konasl.catalog.domain.Product;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.eventsourcing.ConcurrencyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ProductEventStore for MVP.
 * 
 * TODO: Replace with JPA-based implementation for production.
 * 
 * HEXAGONAL ARCHITECTURE:
 * - This is an outbound adapter
 * - Implements port defined in application layer
 */
@Repository
public class InMemoryProductEventStore implements ProductEventStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryProductEventStore.class);

    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public InMemoryProductEventStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(Product aggregate) {
        String aggregateId = aggregate.getAggregateId();
        long currentVersion = eventStreams.getOrDefault(aggregateId, List.of()).size();

        // Optimistic locking check
        long expectedVersion = aggregate.getVersion() - aggregate.getUncommittedEvents().size();
        if (currentVersion != expectedVersion) {
            logger.error("Concurrency conflict for product: {} (expected: {}, actual: {})",
                    aggregateId, expectedVersion, currentVersion);
            throw new ConcurrencyException(aggregateId, expectedVersion, currentVersion);
        }

        // Append new events
        List<DomainEvent> events = eventStreams.computeIfAbsent(aggregateId, k -> new ArrayList<>());
        events.addAll(aggregate.getUncommittedEvents());

        aggregate.markEventsAsCommitted();

        logger.info("Saved {} events for product: {}", aggregate.getUncommittedEvents().size(), aggregateId);
    }

    @Override
    public Product load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        if (events == null || events.isEmpty()) {
            logger.warn("No events found for product: {}", aggregateId);
            return null;
        }

        Product product = new Product();
        product.loadFromHistory(events);

        logger.debug("Loaded product from {} events: {}", events.size(), aggregateId);
        return product;
    }

    @Override
    public boolean exists(String aggregateId) {
        return eventStreams.containsKey(aggregateId) && !eventStreams.get(aggregateId).isEmpty();
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(eventStreams.getOrDefault(aggregateId, List.of()));
    }

    @Override
    public List<DomainEvent> getEventsFromVersion(String aggregateId, long fromVersion) {
        List<DomainEvent> allEvents = eventStreams.getOrDefault(aggregateId, List.of());
        if (fromVersion >= allEvents.size()) {
            return List.of();
        }
        return new ArrayList<>(allEvents.subList((int) fromVersion, allEvents.size()));
    }
}
