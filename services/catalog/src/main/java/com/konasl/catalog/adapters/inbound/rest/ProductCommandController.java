package com.konasl.catalog.adapters.inbound.rest;

import com.konasl.catalog.application.command.CreateProductCommand;
import com.konasl.catalog.application.command.PublishProductCommand;
import com.konasl.catalog.application.command.UnpublishProductCommand;
import com.konasl.catalog.application.handler.CreateProductCommandHandler;
import com.konasl.catalog.application.handler.PublishProductCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for product command operations.
 * 
 * HEXAGONAL ARCHITECTURE:
 * - This is an inbound adapter
 * - Delegates to command handlers in application layer
 * - No business logic here
 */
@RestController
@RequestMapping("/api/catalog/products")
public class ProductCommandController {

    private static final Logger logger = LoggerFactory.getLogger(ProductCommandController.class);

    private final CreateProductCommandHandler createHandler;
    private final PublishProductCommandHandler publishHandler;

    public ProductCommandController(CreateProductCommandHandler createHandler,
            PublishProductCommandHandler publishHandler) {
        this.createHandler = createHandler;
        this.publishHandler = publishHandler;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        logger.info("Creating product: {}", request.name());

        String productId = UUID.randomUUID().toString();
        CreateProductCommand command = new CreateProductCommand(
                productId,
                request.name(),
                request.description(),
                request.category(),
                request.imageUrl(),
                "system" // TODO: Get from security context
        );

        createHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ProductResponse(productId, "Product created successfully"));
    }

    @PostMapping("/{productId}/publish")
    public ResponseEntity<ProductResponse> publishProduct(@PathVariable String productId) {
        logger.info("Publishing product: {}", productId);

        PublishProductCommand command = new PublishProductCommand(
                productId,
                "system" // TODO: Get from security context
        );

        publishHandler.handle(command);

        return ResponseEntity.ok(new ProductResponse(productId, "Product published successfully"));
    }

    /**
     * Request DTO for creating a product.
     */
    public record CreateProductRequest(
            String name,
            String description,
            String category,
            String imageUrl) {
    }

    /**
     * Response DTO for product operations.
     */
    public record ProductResponse(
            String productId,
            String message) {
    }
}
