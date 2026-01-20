package com.konasl.payments.application.query;

import com.konasl.common.cqrs.Query;

import java.util.UUID;

/**
 * Query to get payment by ID.
 */
public record GetPaymentQuery(String paymentId) implements Query<PaymentDto> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
