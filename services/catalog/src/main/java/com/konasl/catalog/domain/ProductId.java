package com.konasl.catalog.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique product identifier.
 * Immutable and type-safe.
 */
public record ProductId(String value) {

    public ProductId {
        Objects.requireNonNull(value, "Product ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be blank");
        }
    }

    /**
     * Generates a new unique product ID.
     */
    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    /**
     * Creates a ProductId from a string value.
     */
    public static ProductId of(String value) {
        return new ProductId(value);
    }
}
