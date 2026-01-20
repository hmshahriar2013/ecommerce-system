package com.konasl.catalog.domain;

import java.util.Objects;

/**
 * Value object representing a product category.
 * MVP: Single category per product.
 */
public record Category(String name) {

    public Category {
        Objects.requireNonNull(name, "Category name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
    }

    /**
     * Creates a Category from a string value.
     */
    public static Category of(String name) {
        return new Category(name);
    }
}
