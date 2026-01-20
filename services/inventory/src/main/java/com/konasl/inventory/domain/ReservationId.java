package com.konasl.inventory.domain;

import java.util.Objects;

/**
 * Value object representing a reservation identifier.
 */
public record ReservationId(String value) {

    public ReservationId {
        Objects.requireNonNull(value, "Reservation ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Reservation ID cannot be blank");
        }
    }

    public static ReservationId of(String value) {
        return new ReservationId(value);
    }
}
