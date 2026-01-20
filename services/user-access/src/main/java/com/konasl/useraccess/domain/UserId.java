package com.konasl.useraccess.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a user identifier.
 */
public record UserId(String value) {

    public UserId {
        Objects.requireNonNull(value, "User ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be blank");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
