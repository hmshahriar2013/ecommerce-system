package com.konasl.inventory.domain;

/**
 * Exception thrown when attempting to reserve more stock than available.
 * Prevents overselling.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
