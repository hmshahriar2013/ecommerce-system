package com.konasl.catalog.adapters.outbound.persistence;

import com.konasl.catalog.application.port.CatalogOutboxRepository;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.common.outbox.OutboxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of CatalogOutboxRepository for MVP.
 * 
 * TODO: Replace with JPA-based implementation for production.
 */
@Repository
public class InMemoryOutboxRepository implements CatalogOutboxRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOutboxRepository.class);

    private final Map<String, OutboxEvent> outboxEvents = new ConcurrentHashMap<>();

    @Override
    public void save(OutboxEvent event) {
        outboxEvents.put(event.getId(), event);
        logger.debug("Saved outbox event: {}", event.getId());
    }

    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        return outboxEvents.values().stream()
                .filter(event -> event.getStatus() == OutboxStatus.PENDING)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void update(OutboxEvent event) {
        outboxEvents.put(event.getId(), event);
        logger.debug("Updated outbox event: {} (status: {})", event.getId(), event.getStatus());
    }

    @Override
    public void deleteDispatchedEventsBefore(Instant timestamp) {
        List<String> toDelete = new ArrayList<>();
        outboxEvents.forEach((id, event) -> {
            if (event.getStatus() == OutboxStatus.DISPATCHED &&
                    event.getDispatchedAt() != null &&
                    event.getDispatchedAt().isBefore(timestamp)) {
                toDelete.add(id);
            }
        });

        toDelete.forEach(outboxEvents::remove);
        logger.info("Deleted {} dispatched outbox events", toDelete.size());
    }
}
