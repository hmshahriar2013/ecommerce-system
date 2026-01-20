package com.konasl.inventory.domain;

/**
 * Status of stock for a product.
 */
public enum StockStatus {
    /**
     * Stock is available for reservation.
     */
    AVAILABLE,

    /**
     * Stock is low (below threshold).
     */
    LOW_STOCK,

    /**
     * Stock is out of stock (quantity is zero).
     */
    OUT_OF_STOCK
}
