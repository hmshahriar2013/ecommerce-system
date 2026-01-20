package com.konasl.cart.application.query;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing cart details for query results.
 */
public record CartDto(
        String cartId,
        String userId,
        List<CartItemDto> items,
        BigDecimal totalAmount) {

    public record CartItemDto(
            String productId,
            int quantity,
            BigDecimal price) {
    }
}
