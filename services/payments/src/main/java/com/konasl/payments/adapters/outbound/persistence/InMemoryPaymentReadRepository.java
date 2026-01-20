package com.konasl.payments.adapters.outbound.persistence;

import com.konasl.payments.application.port.PaymentReadRepository;
import com.konasl.payments.application.query.PaymentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of payment read repository.
 */
@Repository
public class InMemoryPaymentReadRepository implements PaymentReadRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryPaymentReadRepository.class);

    private final ConcurrentHashMap<String, PaymentDto> payments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> orderIdToPaymentId = new ConcurrentHashMap<>();

    @Override
    public Optional<PaymentDto> findById(String paymentId) {
        logger.debug("Finding payment by ID: {}", paymentId);
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public Optional<PaymentDto> findByOrderId(String orderId) {
        logger.debug("Finding payment by order ID: {}", orderId);
        String paymentId = orderIdToPaymentId.get(orderId);
        return paymentId != null ? Optional.ofNullable(payments.get(paymentId)) : Optional.empty();
    }

    @Override
    public void save(PaymentDto paymentDto) {
        payments.put(paymentDto.paymentId(), paymentDto);
        orderIdToPaymentId.put(paymentDto.orderId(), paymentDto.paymentId());
        logger.debug("Saved payment DTO: {}", paymentDto.paymentId());
    }
}
