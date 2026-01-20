package com.konasl.cart.domain;

import java.util.Objects;

/**
 * Value object representing a cart item.
 */
public record CartItem(
        ProductId productId,
        int quantity) {

    public CartItem {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(productId, newQuantity);
    }
}
