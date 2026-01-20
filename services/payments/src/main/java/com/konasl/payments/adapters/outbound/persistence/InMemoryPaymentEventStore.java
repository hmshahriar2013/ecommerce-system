package com.konasl.payments.adapters.outbound.persistence;

import com.konasl.common.domain.DomainEvent;
import com.konasl.payments.application.port.PaymentEventStore;
import com.konasl.payments.domain.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of Payment event store.
 */
@Repository
public class InMemoryPaymentEventStore implements PaymentEventStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryPaymentEventStore.class);

    private final ConcurrentHashMap<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    @Override
    public void save(Payment aggregate) {
        String aggregateId = aggregate.getAggregateId();

        eventStreams.compute(aggregateId, (id, existingEvents) -> {
            List<DomainEvent> events = existingEvents != null ? new ArrayList<>(existingEvents) : new ArrayList<>();
            events.addAll(aggregate.getUncommittedEvents());

            logger.debug("Saved {} events for payment aggregate: {}",
                    aggregate.getUncommittedEvents().size(), aggregateId);

            return events;
        });
    }

    @Override
    public Payment load(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);

        if (events == null || events.isEmpty()) {
            return null;
        }

        Payment payment = new Payment();
        payment.loadFromHistory(events);

        logger.debug("Loaded payment aggregate: {} from {} events", aggregateId, events.size());

        return payment;
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

        if (allEvents == null || allEvents.isEmpty()) {
            return new ArrayList<>();
        }

        return allEvents.stream()
                .skip(fromVersion)
                .toList();
    }
}
