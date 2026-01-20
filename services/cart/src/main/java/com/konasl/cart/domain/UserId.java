package com.konasl.cart.domain;

import java.util.Objects;

/**
 * Value object representing user identifier.
 */
public record UserId(String value) {

    public UserId {
        Objects.requireNonNull(value, "User ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be blank");
        }
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
