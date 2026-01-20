package com.konasl.catalog.application.query;

/**
 * DTO for product read model.
 */
public record ProductDto(
        String productId,
        String name,
        String description,
        String category,
        String imageUrl,
        String status) {
}
