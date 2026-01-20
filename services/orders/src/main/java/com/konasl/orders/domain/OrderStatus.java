package com.konasl.orders.domain;

/**
 * Order status in the system.
 */
public enum OrderStatus {
    /**
     * Order created, awaiting payment.
     */
    PENDING_PAYMENT,

    /**
     * Payment confirmed, order processing.
     */
    CONFIRMED,

    /**
     * Order cancelled.
     */
    CANCELLED,

    /**
     * Order shipped.
     */
    SHIPPED,

    /**
     * Order delivered.
     */
    DELIVERED
}
