package com.konasl.orders.application.query;

import java.util.List;

/**
 * DTO for order read model.
 */
public record OrderDto(
        String orderId,
        String userId,
        List<OrderItemDto> items,
        String totalAmount,
        String status) {
    public record OrderItemDto(
            String productId,
            int quantity,
            String priceAtOrder) {
    }
}
