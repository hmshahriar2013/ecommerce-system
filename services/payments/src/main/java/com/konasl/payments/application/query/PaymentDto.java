package com.konasl.payments.application.query;

import java.math.BigDecimal;

/**
 * DTO for payment read model.
 */
public record PaymentDto(
        String paymentId,
        String orderId,
        BigDecimal amount,
        String currency,
        String status) {
}
