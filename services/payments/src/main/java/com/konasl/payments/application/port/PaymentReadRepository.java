package com.konasl.payments.application.port;

import com.konasl.payments.application.query.PaymentDto;

import java.util.Optional;

/**
 * Read repository port for Payment queries.
 */
public interface PaymentReadRepository {

    /**
     * Find payment by ID.
     */
    Optional<PaymentDto> findById(String paymentId);

    /**
     * Find payment by order ID.
     */
    Optional<PaymentDto> findByOrderId(String orderId);

    /**
     * Save or update payment DTO.
     */
    void save(PaymentDto paymentDto);
}
