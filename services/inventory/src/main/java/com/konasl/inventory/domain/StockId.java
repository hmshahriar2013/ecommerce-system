package com.konasl.inventory.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a stock item identifier.
 */
public record StockId(String value) {

    public StockId {
        Objects.requireNonNull(value, "Stock ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Stock ID cannot be blank");
        }
    }

    public static StockId generate() {
        return new StockId(UUID.randomUUID().toString());
    }

    public static StockId of(String value) {
        return new StockId(value);
    }
}
