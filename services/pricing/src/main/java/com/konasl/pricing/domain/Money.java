package com.konasl.pricing.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object representing monetary amount.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money usd(BigDecimal amount) {
        return new Money(amount, "USD");
    }
}
