package com.konasl.payments.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payment aggregate root in the Payments bounded context.
 * 
 * BUSINESS RULES:
 * - Payments are associated with orders
 * - Successful payments trigger order confirmation
 * - Failed payments keep order in PENDING_PAYMENT status
 * - Payments start with PENDING status
 */
public class Payment extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(Payment.class);

    private PaymentId paymentId;
    private OrderId orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public Payment() {
        super();
    }

    /**
     * Processes a payment (command method).
     * In MVP, automatically succeeds for testing purposes.
     */
    public Payment(PaymentId paymentId, OrderId orderId, BigDecimal amount, String currency, String processedBy) {
        super();

        validatePayment(amount);

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        setAggregateId(paymentId.value());

        // Auto-succeed for MVP
        completeSuccessfully(processedBy);

        logger.info("Payment processed for order {}: {} {}", orderId.value(), amount, currency);
    }

    /**
     * Marks payment as successful.
     */
    private void completeSuccessfully(String completedBy) {
        raiseEvent(new PaymentSuccessfulEvent(
                UUID.randomUUID().toString(),
                paymentId.value(),
                orderId.value(),
                amount,
                currency,
                Instant.now(),
                completedBy));

        logger.info("Payment successful: {} for order {}", paymentId.value(), orderId.value());
    }

    /**
     * Applies events to rebuild aggregate state.
     */
    @Override
    protected void apply(DomainEvent event) {
        if (event instanceof PaymentSuccessfulEvent e) {
            applyPaymentSuccessful(e);
        } else {
            logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyPaymentSuccessful(PaymentSuccessfulEvent event) {
        this.status = PaymentStatus.SUCCESSFUL;
    }

    private void validatePayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

    // Getters

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
