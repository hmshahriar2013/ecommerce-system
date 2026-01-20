package com.konasl.cart.adapters.outbound.persistence;

import com.konasl.cart.application.port.CartEventStore;
import com.konasl.cart.domain.Cart;
import com.konasl.common.domain.DomainEvent;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of CartEventStore.
 */
@Repository
public class InMemoryCartEventStore implements CartEventStore {

    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    @Override
    public void save(Cart aggregate) {
        List<DomainEvent> uncommittedEvents = aggregate.getUncommittedEvents();
        if (!uncommittedEvents.isEmpty()) {
            eventStreams.computeIfAbsent(aggregate.getAggregateId(), k -> new ArrayList<>())
                    .addAll(uncommittedEvents);
        }
    }

    @Override
    public Cart load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        if (events == null || events.isEmpty()) {
            throw new RuntimeException("Cart not found: " + aggregateId);
        }

        Cart cart = new Cart();
        cart.loadFromHistory(events);
        return cart;
    }

    @Override
    public boolean exists(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        return events != null && !events.isEmpty();
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(eventStreams.getOrDefault(aggregateId, new ArrayList<>()));
    }

    @Override
    public List<DomainEvent> getEventsFromVersion(String aggregateId, long fromVersion) {
        List<DomainEvent> allEvents = eventStreams.getOrDefault(aggregateId, new ArrayList<>());
        return allEvents.stream()
                .skip(fromVersion)
                .collect(Collectors.toList());
    }
}
