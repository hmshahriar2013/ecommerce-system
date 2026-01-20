package com.konasl.orders.adapters.outbound.persistence;

import com.konasl.common.domain.DomainEvent;
import com.konasl.orders.application.port.OrderEventStore;
import com.konasl.orders.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of Order event store.
 */
@Repository
public class InMemoryOrderEventStore implements OrderEventStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrderEventStore.class);

    private final ConcurrentHashMap<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    @Override
    public void save(Order aggregate) {
        String aggregateId = aggregate.getAggregateId();

        eventStreams.compute(aggregateId, (id, existingEvents) -> {
            List<DomainEvent> events = existingEvents != null ? new ArrayList<>(existingEvents) : new ArrayList<>();
            events.addAll(aggregate.getUncommittedEvents());

            logger.debug("Saved {} events for order aggregate: {}",
                    aggregate.getUncommittedEvents().size(), aggregateId);

            return events;
        });
    }

    @Override
    public Order load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);

        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + aggregateId);
        }

        Order order = new Order();
        order.loadFromHistory(events);

        logger.debug("Loaded order aggregate: {} from {} events", aggregateId, events.size());

        return order;
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
        List<DomainEvent> allEvents = eventStreams.getOrDefault(aggregateId, new ArrayList<>());
        // Version is 1-indexed, so version 1 = index 0
        int startIndex = (int) Math.max(0, fromVersion - 1);
        if (startIndex >= allEvents.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(allEvents.subList(startIndex, allEvents.size()));
    }
}
