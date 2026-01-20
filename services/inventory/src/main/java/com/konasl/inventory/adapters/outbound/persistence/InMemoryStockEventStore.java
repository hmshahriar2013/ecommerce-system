package com.konasl.inventory.adapters.outbound.persistence;

import com.konasl.common.domain.DomainEvent;
import com.konasl.common.eventsourcing.ConcurrencyException;
import com.konasl.inventory.application.port.StockEventStore;
import com.konasl.inventory.domain.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of Stock event store.
 */
@Repository
public class InMemoryStockEventStore implements StockEventStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStockEventStore.class);

    private final ConcurrentHashMap<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    @Override
    public void save(Stock aggregate) {
        String aggregateId = aggregate.getAggregateId();

        eventStreams.compute(aggregateId, (id, existingEvents) -> {
            List<DomainEvent> events = existingEvents != null ? new ArrayList<>(existingEvents) : new ArrayList<>();

            // Add new uncommitted events
            events.addAll(aggregate.getUncommittedEvents());

            logger.debug("Saved {} events for stock aggregate: {}",
                    aggregate.getUncommittedEvents().size(), aggregateId);

            return events;
        });
    }

    @Override
    public Stock load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);

        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Stock not found: " + aggregateId);
        }

        Stock stock = new Stock();
        stock.loadFromHistory(events);

        logger.debug("Loaded stock aggregate: {} from {} events", aggregateId, events.size());

        return stock;
    }

    @Override
    public boolean exists(String aggregateId) {
        return eventStreams.containsKey(aggregateId) && !eventStreams.get(aggregateId).isEmpty();
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(eventStreams.getOrDefault(aggregateId, new ArrayList<>()));
    }

    @Override
    public List<DomainEvent> getEventsFromVersion(String aggregateId, long fromVersion) {
        List<DomainEvent> allEvents = eventStreams.get(aggregateId);
        if (allEvents == null) {
            return new ArrayList<>();
        }

        return allEvents.stream()
                .skip(fromVersion)
                .toList();
    }
}
