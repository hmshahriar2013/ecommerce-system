package com.konasl.fulfillment.adapters.outbound.persistence;

import com.konasl.common.domain.DomainEvent;
import com.konasl.fulfillment.application.port.ShipmentEventStore;
import com.konasl.fulfillment.domain.Shipment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ShipmentEventStore.
 */
@Repository
public class InMemoryShipmentEventStore implements ShipmentEventStore {

    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    @Override
    public void save(Shipment aggregate) {
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventStreams.computeIfAbsent(aggregate.getAggregateId(), k -> new ArrayList<>()).addAll(events);
    }

    @Override
    public Shipment load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        if (events == null || events.isEmpty()) {
            throw new RuntimeException("Shipment not found: " + aggregateId);
        }

        Shipment shipment = new Shipment();
        shipment.loadFromHistory(events);
        return shipment;
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
