package com.konasl.catalog.adapters.inbound.event;

import com.konasl.catalog.application.port.ProductReadRepository;
import com.konasl.catalog.application.query.ProductDto;
import com.konasl.catalog.domain.ProductCreatedEvent;
import com.konasl.catalog.domain.ProductPublishedEvent;
import com.konasl.catalog.domain.ProductUnpublishedEvent;
import com.konasl.catalog.domain.ProductUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event handler that updates the product read model.
 * 
 * EVENTUAL CONSISTENCY:
 * - Listen to domain events
 * - Update denormalized read model
 * - Optimized for queries
 * 
 * MVP: Using in-process events (Spring ApplicationEventPublisher)
 * TODO: Replace with RabbitMQ consumer for distributed architecture
 */
@Component
public class ProductReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(ProductReadModelProjection.class);

    private final ProductReadRepository readRepository;

    public ProductReadModelProjection(ProductReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @EventListener
    public void on(ProductCreatedEvent event) {
        logger.info("Projecting ProductCreatedEvent to read model: {}", event.aggregateId());

        ProductDto productDto = new ProductDto(
                event.aggregateId(),
                event.name(),
                event.description(),
                event.category(),
                event.imageUrl(),
                "DRAFT");

        readRepository.save(productDto);
    }

    @EventListener
    public void on(ProductPublishedEvent event) {
        logger.info("Projecting ProductPublishedEvent to read model: {}", event.aggregateId());

        readRepository.findById(event.aggregateId()).ifPresent(existing -> {
            ProductDto updated = new ProductDto(
                    existing.productId(),
                    existing.name(),
                    existing.description(),
                    existing.category(),
                    existing.imageUrl(),
                    "PUBLISHED");
            readRepository.save(updated);
        });
    }

    @EventListener
    public void on(ProductUnpublishedEvent event) {
        logger.info("Projecting ProductUnpublishedEvent to read model: {}", event.aggregateId());

        readRepository.findById(event.aggregateId()).ifPresent(existing -> {
            ProductDto updated = new ProductDto(
                    existing.productId(),
                    existing.name(),
                    existing.description(),
                    existing.category(),
                    existing.imageUrl(),
                    "UNPUBLISHED");
            readRepository.save(updated);
        });
    }

    @EventListener
    public void on(ProductUpdatedEvent event) {
        logger.info("Projecting ProductUpdatedEvent to read model: {}", event.aggregateId());

        readRepository.findById(event.aggregateId()).ifPresent(existing -> {
            ProductDto updated = new ProductDto(
                    existing.productId(),
                    event.name(),
                    event.description(),
                    event.category(),
                    event.imageUrl(),
                    existing.status());
            readRepository.save(updated);
        });
    }
}
