package com.konasl.pricing.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing product identifier.
 */
public record ProductId(String value) {

    public ProductId {
        Objects.requireNonNull(value, "Product ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be blank");
        }
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }
}
