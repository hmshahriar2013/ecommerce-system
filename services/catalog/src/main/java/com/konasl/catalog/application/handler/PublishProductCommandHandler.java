package com.konasl.catalog.application.handler;

import com.konasl.catalog.application.command.PublishProductCommand;
import com.konasl.catalog.application.port.CatalogOutboxRepository;
import com.konasl.catalog.application.port.ProductEventStore;
import com.konasl.catalog.domain.Product;
import com.konasl.catalog.infrastructure.config.DomainEventPublisher;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.outbox.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handler for PublishProductCommand.
 */
@Component
public class PublishProductCommandHandler implements CommandHandler<PublishProductCommand> {

    private static final Logger logger = LoggerFactory.getLogger(PublishProductCommandHandler.class);

    private final ProductEventStore eventStore;
    private final CatalogOutboxRepository outboxRepository;
    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public PublishProductCommandHandler(ProductEventStore eventStore,
            CatalogOutboxRepository outboxRepository,
            DomainEventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public void handle(PublishProductCommand command) {
        logger.info("Publishing product: {}", command.productId());

        // Load aggregate from event store
        Product product = eventStore.load(command.productId());
        if (product == null) {
            logger.error("Product not found: {}", command.productId());
            throw new IllegalArgumentException("Product not found: " + command.productId());
        }

        // Execute business logic
        product.publish(command.publishedBy());

        // Save events
        eventStore.save(product);

        // Publish events for read model projection
        product.getUncommittedEvents().forEach(eventPublisher::publish);

        // Save to outbox
        product.getUncommittedEvents().forEach(event -> {
            try {
                String eventPayload = objectMapper.writeValueAsString(event);
                OutboxEvent outboxEvent = OutboxEvent.fromDomainEvent(
                        UUID.randomUUID().toString(),
                        event,
                        eventPayload);
                outboxRepository.save(outboxEvent);
            } catch (Exception e) {
                logger.error("Failed to serialize event for outbox", e);
                throw new RuntimeException("Failed to save event to outbox", e);
            }
        });

        logger.info("Product published successfully: {}", command.productId());
    }

    @Override
    public Class<PublishProductCommand> getCommandType() {
        return PublishProductCommand.class;
    }
}
