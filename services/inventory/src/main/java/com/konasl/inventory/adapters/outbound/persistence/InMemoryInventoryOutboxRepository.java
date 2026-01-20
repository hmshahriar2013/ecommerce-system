package com.konasl.inventory.adapters.outbound.persistence;

import com.konasl.common.outbox.OutboxEvent;
import com.konasl.common.outbox.OutboxStatus;
import com.konasl.inventory.application.port.InventoryOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of inventory outbox repository.
 */
@Repository
public class InMemoryInventoryOutboxRepository implements InventoryOutboxRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryInventoryOutboxRepository.class);

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
        events.entrySet().removeIf(entry -> entry.getValue().getStatus() == OutboxStatus.DISPATCHED &&
                entry.getValue().getDispatchedAt() != null &&
                entry.getValue().getDispatchedAt().isBefore(timestamp));
        logger.debug("Deleted dispatched events before: {}", timestamp);
    }
}
