package com.konasl.catalog.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Product aggregate root in the Catalog bounded context.
 * 
 * BUSINESS RULES:
 * - Product must have a name and description
 * - Product can only be published if it has all required information
 * - Published products can be unpublished
 * - Unpublished products can be published again
 * 
 * HEXAGONAL ARCHITECTURE:
 * - This is pure domain logic with NO framework dependencies
 * - All state changes produce domain events
 */
public class Product extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(Product.class);

    private ProductId productId;
    private String name;
    private String description;
    private Category category;
    private String imageUrl;
    private ProductStatus status;

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public Product() {
        super();
    }

    /**
     * Creates a new product (command method).
     */
    public Product(ProductId productId, String name, String description,
            Category category, String imageUrl, String createdBy) {
        super();

        validateProductCreation(name, description);

        raiseEvent(new ProductCreatedEvent(
                UUID.randomUUID().toString(),
                productId.value(),
                name,
                description,
                category.name(),
                imageUrl,
                Instant.now(),
                createdBy));

        logger.info("Product created: {}", productId.value());
    }

    /**
     * Publishes the product, making it visible to customers.
     */
    public void publish(String publishedBy) {
        if (status == ProductStatus.PUBLISHED) {
            logger.warn("Product {} is already published", productId.value());
            throw new IllegalStateException("Product is already published");
        }

        validateProductForPublishing();

        raiseEvent(new ProductPublishedEvent(
                UUID.randomUUID().toString(),
                productId.value(),
                Instant.now(),
                publishedBy));

        logger.info("Product published: {}", productId.value());
    }

    /**
     * Unpublishes the product, hiding it from customers.
     */
    public void unpublish(String unpublishedBy) {
        if (status != ProductStatus.PUBLISHED) {
            logger.warn("Product {} is not published, cannot unpublish", productId.value());
            throw new IllegalStateException("Product is not published");
        }

        raiseEvent(new ProductUnpublishedEvent(
                UUID.randomUUID().toString(),
                productId.value(),
                Instant.now(),
                unpublishedBy));

        logger.info("Product unpublished: {}", productId.value());
    }

    /**
     * Updates product information.
     */
    public void updateDetails(String name, String description, Category category,
            String imageUrl, String updatedBy) {
        validateProductUpdate(name, description);

        raiseEvent(new ProductUpdatedEvent(
                UUID.randomUUID().toString(),
                productId.value(),
                name,
                description,
                category.name(),
                imageUrl,
                Instant.now(),
                updatedBy));

        logger.info("Product updated: {}", productId.value());
    }

    /**
     * Applies events to rebuild aggregate state.
     * Used for event sourcing reconstruction.
     */
    @Override
    protected void apply(DomainEvent event) {
        switch (event) {
            case ProductCreatedEvent e -> applyProductCreated(e);
            case ProductPublishedEvent e -> applyProductPublished(e);
            case ProductUnpublishedEvent e -> applyProductUnpublished(e);
            case ProductUpdatedEvent e -> applyProductUpdated(e);
            default -> logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyProductCreated(ProductCreatedEvent event) {
        this.productId = ProductId.of(event.aggregateId());
        this.name = event.name();
        this.description = event.description();
        this.category = Category.of(event.category());
        this.imageUrl = event.imageUrl();
        this.status = ProductStatus.DRAFT;
        setAggregateId(event.aggregateId());
    }

    private void applyProductPublished(ProductPublishedEvent event) {
        this.status = ProductStatus.PUBLISHED;
    }

    private void applyProductUnpublished(ProductUnpublishedEvent event) {
        this.status = ProductStatus.UNPUBLISHED;
    }

    private void applyProductUpdated(ProductUpdatedEvent event) {
        this.name = event.name();
        this.description = event.description();
        this.category = Category.of(event.category());
        this.imageUrl = event.imageUrl();
    }

    // Validation methods

    private void validateProductCreation(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Product description cannot be empty");
        }
    }

    private void validateProductUpdate(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Product description cannot be empty");
        }
    }

    private void validateProductForPublishing() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Cannot publish product without name");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalStateException("Cannot publish product without description");
        }
        if (category == null) {
            throw new IllegalStateException("Cannot publish product without category");
        }
    }

    // Getters

    public ProductId getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public boolean isPublished() {
        return status == ProductStatus.PUBLISHED;
    }
}
