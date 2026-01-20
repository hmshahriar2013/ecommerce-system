package com.konasl.fulfillment.domain;

/**
 * Shipment status in the system.
 */
public enum ShipmentStatus {
    /**
     * Shipment ready to ship.
     */
    READY_TO_SHIP,

    /**
     * Shipment in transit.
     */
    SHIPPED,

    /**
     * Shipment delivered.
     */
    DELIVERED,

    /**
     * Shipment cancelled.
     */
    CANCELLED
}
