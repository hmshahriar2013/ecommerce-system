package com.konasl.inventory.application.query;

/**
 * DTO for stock read model.
 */
public record StockDto(
        String productId,
        int availableQuantity,
        int reservedQuantity,
        int totalQuantity,
        String status) {
}
