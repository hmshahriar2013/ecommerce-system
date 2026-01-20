package com.konasl.payments.adapters.inbound.event;

import com.konasl.payments.application.port.PaymentReadRepository;
import com.konasl.payments.application.query.PaymentDto;
import com.konasl.payments.domain.PaymentSuccessfulEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Projects payment domain events to read model.
 */
@Component
public class PaymentReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(PaymentReadModelProjection.class);

    private final PaymentReadRepository repository;

    public PaymentReadModelProjection(PaymentReadRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onPaymentSuccessful(PaymentSuccessfulEvent event) {
        logger.info("Projecting PaymentSuccessfulEvent: {}", event.aggregateId());

        PaymentDto dto = new PaymentDto(
                event.aggregateId(),
                event.orderId(),
                event.amount(),
                event.currency(),
                "SUCCESSFUL");

        repository.save(dto);
    }
}
