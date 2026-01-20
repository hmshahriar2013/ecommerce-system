package com.konasl.orders.adapters.outbound.persistence;

import com.konasl.common.outbox.OutboxEvent;
import com.konasl.common.outbox.OutboxStatus;
import com.konasl.orders.application.port.OrdersOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of orders outbox repository.
 */
@Repository
public class InMemoryOrdersOutboxRepository implements OrdersOutboxRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrdersOutboxRepository.class);

    private final ConcurrentHashMap<String, OutboxEvent> events = new ConcurrentHashMap<>();

    @Override
    public void save(OutboxEvent event) {
        events.put(event.getId(), event);
        logger.debug("Saved outbox event: {} of type: {}", event.getId(), event.getEventType());
    }

    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        return events.values().stream()
                .filter(event -> event.getStatus() == OutboxStatus.PENDING)
                .limit(limit)
                .toList();
    }

    @Override
    public void update(OutboxEvent event) {
        events.put(event.getId(), event);
        logger.debug("Updated outbox event: {}", event.getId());
    }

    @Override
    public void deleteDispatchedEventsBefore(java.time.Instant timestamp) {
        events.entrySet().removeIf(entry -> {
            OutboxEvent event = entry.getValue();
            return event.getStatus() == OutboxStatus.DISPATCHED
                    && event.getDispatchedAt() != null
                    && event.getDispatchedAt().isBefore(timestamp);
        });
        logger.debug("Deleted dispatched events before: {}", timestamp);
    }
}
