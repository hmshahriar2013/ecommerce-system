package com.konasl.orders.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing order identifier.
 */
public record OrderId(String value) {

    public OrderId {
        Objects.requireNonNull(value, "Order ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be blank");
        }
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }
}
