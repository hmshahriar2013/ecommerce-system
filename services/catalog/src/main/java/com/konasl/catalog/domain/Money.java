package com.konasl.catalog.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object representing a monetary amount with currency.
 * Immutable and type-safe.
 * 
 * MVP: Single currency (USD) for simplicity.
 */
public record Money(BigDecimal amount, String currency) {

    private static final String DEFAULT_CURRENCY = "USD";

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be blank");
        }
    }

    /**
     * Creates a Money object with USD currency.
     */
    public static Money usd(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    /**
     * Creates a Money object with USD currency from a double value.
     */
    public static Money usd(double amount) {
        return new Money(BigDecimal.valueOf(amount), DEFAULT_CURRENCY);
    }

    /**
     * Creates a zero money object.
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    }
}
