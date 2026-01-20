package com.konasl.payments.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing payment identifier.
 */
public record PaymentId(String value) {

    public PaymentId {
        Objects.requireNonNull(value, "Payment ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be blank");
        }
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID().toString());
    }

    public static PaymentId of(String value) {
        return new PaymentId(value);
    }
}
