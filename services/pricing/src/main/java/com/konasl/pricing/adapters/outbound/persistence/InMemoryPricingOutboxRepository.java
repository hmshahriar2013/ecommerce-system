package com.konasl.pricing.adapters.outbound.persistence;

import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.common.outbox.OutboxStatus;
import com.konasl.pricing.application.port.PricingOutboxRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of PricingOutboxRepository.
 */
@Repository
public class InMemoryPricingOutboxRepository implements PricingOutboxRepository {

    private final Map<String, OutboxEvent> outbox = new ConcurrentHashMap<>();

    @Override
    public void save(OutboxEvent event) {
        outbox.put(event.getId(), event);
    }

    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        return outbox.values().stream()
                .filter(event -> event.getStatus() == OutboxStatus.PENDING)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void update(OutboxEvent event) {
        outbox.put(event.getId(), event);
    }

    @Override
    public void deleteDispatchedEventsBefore(Instant timestamp) {
        outbox.entrySet().removeIf(entry -> entry.getValue().getStatus() == OutboxStatus.DISPATCHED &&
                entry.getValue().getCreatedAt().isBefore(timestamp));
    }
}
