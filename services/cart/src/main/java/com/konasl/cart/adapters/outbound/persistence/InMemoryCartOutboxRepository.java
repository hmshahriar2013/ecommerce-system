package com.konasl.cart.adapters.outbound.persistence;

import com.konasl.cart.application.port.CartOutboxRepository;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.common.outbox.OutboxStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of CartOutboxRepository.
 */
@Repository
public class InMemoryCartOutboxRepository implements CartOutboxRepository {

    private final Map<String, OutboxEvent> outbox = new ConcurrentHashMap<>();

    @Override
    public void save(OutboxEvent event) {
        outbox.put(event.getId(), event);
    }

    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        return outbox.values().stream()
                .filter(event -> event.getStatus() != OutboxStatus.DISPATCHED)
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
                entry.getValue().getDispatchedAt() != null &&
                entry.getValue().getDispatchedAt().isBefore(timestamp));
    }
}
