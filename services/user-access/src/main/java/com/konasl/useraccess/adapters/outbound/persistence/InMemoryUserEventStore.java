package com.konasl.useraccess.adapters.outbound.persistence;

import com.konasl.common.domain.DomainEvent;
import com.konasl.useraccess.application.port.UserEventStore;
import com.konasl.useraccess.domain.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of UserEventStore.
 */
@Repository
public class InMemoryUserEventStore implements UserEventStore {

    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    @Override
    public void save(User aggregate) {
        String aggregateId = aggregate.getAggregateId();
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventStreams.computeIfAbsent(aggregateId, k -> new ArrayList<>()).addAll(events);
    }

    @Override
    public User load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        if (events == null || events.isEmpty()) {
            return null;
        }

        User user = new User();
        events.forEach(user::apply);
        user.markEventsAsCommitted();
        return user;
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
        List<DomainEvent> allEvents = eventStreams.get(aggregateId);
        if (allEvents == null) {
            return new ArrayList<>();
        }
        return allEvents.stream()
                .skip(fromVersion)
                .collect(Collectors.toList());
    }
}
