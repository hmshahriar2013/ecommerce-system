package com.konasl.pricing.application.query;

import java.math.BigDecimal;

/**
 * DTO representing price details for query results.
 */
public record PriceDto(
        String productId,
        BigDecimal amount,
        String currency,
        String status) {
}
