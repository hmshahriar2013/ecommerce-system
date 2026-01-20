package com.konasl.inventory.domain;

import java.util.Objects;

/**
 * Value object representing a product identifier from Catalog context.
 */
public record ProductId(String value) {

    public ProductId {
        Objects.requireNonNull(value, "Product ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be blank");
        }
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }
}
