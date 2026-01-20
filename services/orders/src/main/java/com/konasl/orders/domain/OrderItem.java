package com.konasl.orders.domain;

import java.util.Objects;

/**
 * Value object representing an order line item.
 */
public record OrderItem(
        String productId,
        int quantity,
        String priceAtOrder) {

    public OrderItem {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        Objects.requireNonNull(priceAtOrder, "Price cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
