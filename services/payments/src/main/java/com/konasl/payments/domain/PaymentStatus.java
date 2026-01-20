package com.konasl.payments.domain;

/**
 * Payment status in the system.
 */
public enum PaymentStatus {
    /**
     * Payment pending processing.
     */
    PENDING,

    /**
     * Payment successful.
     */
    SUCCESSFUL,

    /**
     * Payment failed.
     */
    FAILED,

    /**
     * Payment refunded.
     */
    REFUNDED
}
