package com.konasl.catalog.application.handler;

import com.konasl.catalog.application.command.CreateProductCommand;
import com.konasl.catalog.application.port.CatalogOutboxRepository;
import com.konasl.catalog.application.port.ProductEventStore;
import com.konasl.catalog.domain.Category;
import com.konasl.catalog.domain.Product;
import com.konasl.catalog.domain.ProductId;
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
 * Handler for CreateProductCommand.
 * 
 * HEXAGONAL ARCHITECTURE:
 * - Application layer orchestrates use case
 * - Delegates to domain for business logic
 * - Uses ports to interact with infrastructure
 */
@Component
public class CreateProductCommandHandler implements CommandHandler<CreateProductCommand> {

    private static final Logger logger = LoggerFactory.getLogger(CreateProductCommandHandler.class);

    private final ProductEventStore eventStore;
    private final CatalogOutboxRepository outboxRepository;
    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public CreateProductCommandHandler(ProductEventStore eventStore,
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
    public void handle(CreateProductCommand command) {
        logger.info("Creating product: {}", command.productId());

        // Check if product already exists
        if (eventStore.exists(command.productId())) {
            logger.error("Product already exists: {}", command.productId());
            throw new IllegalStateException("Product with ID " + command.productId() + " already exists");
        }

        // Create aggregate
        Product product = new Product(
                ProductId.of(command.productId()),
                command.name(),
                command.description(),
                Category.of(command.category()),
                command.imageUrl(),
                command.createdBy());

        // Save events to event store
        eventStore.save(product);

        // Publish events for read model projection
        product.getUncommittedEvents().forEach(eventPublisher::publish);

        // Save events to outbox for messaging
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

        logger.info("Product created successfully: {}", command.productId());
    }

    @Override
    public Class<CreateProductCommand> getCommandType() {
        return CreateProductCommand.class;
    }
}
