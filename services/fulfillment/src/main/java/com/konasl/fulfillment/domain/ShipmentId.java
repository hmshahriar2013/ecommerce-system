package com.konasl.fulfillment.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing shipment identifier.
 */
public record ShipmentId(String value) {

    public ShipmentId {
        Objects.requireNonNull(value, "Shipment ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Shipment ID cannot be blank");
        }
    }

    public static ShipmentId generate() {
        return new ShipmentId(UUID.randomUUID().toString());
    }

    public static ShipmentId of(String value) {
        return new ShipmentId(value);
    }
}
