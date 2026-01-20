package com.konasl.pricing.adapters.outbound.persistence;

import com.konasl.common.domain.DomainEvent;
import com.konasl.pricing.application.port.PriceEventStore;
import com.konasl.pricing.domain.Price;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of PriceEventStore.
 */
@Repository
public class InMemoryPriceEventStore implements PriceEventStore {

    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    @Override
    public void save(Price aggregate) {
        String aggregateId = aggregate.getAggregateId();
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventStreams.computeIfAbsent(aggregateId, k -> new ArrayList<>()).addAll(events);
    }

    @Override
    public Price load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        if (events == null || events.isEmpty()) {
            throw new RuntimeException("Price not found: " + aggregateId);
        }

        Price price = new Price();
        events.forEach(price::apply);
        return price;
    }

    @Override
    public boolean exists(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        return events != null && !events.isEmpty();
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return eventStreams.getOrDefault(aggregateId, new ArrayList<>());
    }

    @Override
    public List<DomainEvent> getEventsFromVersion(String aggregateId, long fromVersion) {
        List<DomainEvent> allEvents = eventStreams.get(aggregateId);

        if (allEvents == null || allEvents.isEmpty()) {
            return new ArrayList<>();
        }

        return allEvents.stream()
                .skip(fromVersion)
                .collect(Collectors.toList());
    }
}
