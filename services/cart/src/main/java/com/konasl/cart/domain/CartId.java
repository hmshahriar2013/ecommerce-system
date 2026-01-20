package com.konasl.cart.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing cart identifier.
 */
public record CartId(String value) {

    public CartId {
        Objects.requireNonNull(value, "Cart ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Cart ID cannot be blank");
        }
    }

    public static CartId generate() {
        return new CartId(UUID.randomUUID().toString());
    }

    public static CartId of(String value) {
        return new CartId(value);
    }
}
