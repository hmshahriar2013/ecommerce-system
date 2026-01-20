package com.konasl.payments.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.payments.application.command.ProcessPaymentCommand;
import com.konasl.payments.application.port.PaymentEventStore;
import com.konasl.payments.application.port.PaymentsOutboxRepository;
import com.konasl.payments.domain.OrderId;
import com.konasl.payments.domain.Payment;
import com.konasl.payments.domain.PaymentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles payment processing.
 */
@Service
public class ProcessPaymentCommandHandler implements CommandHandler<ProcessPaymentCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPaymentCommandHandler.class);

    private final PaymentEventStore eventStore;
    private final PaymentsOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ProcessPaymentCommandHandler(
            PaymentEventStore eventStore,
            PaymentsOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public void handle(ProcessPaymentCommand command) {
        logger.info("Processing payment for order: {} amount: {} {}",
                command.orderId(), command.amount(), command.currency());

        // Create Payment aggregate
        Payment payment = new Payment(
                PaymentId.generate(),
                OrderId.of(command.orderId()),
                command.amount(),
                command.currency(),
                "system");

        // Save to event store
        eventStore.save(payment);

        // Save to outbox
        for (DomainEvent event : payment.getUncommittedEvents()) {
            try {
                String eventPayload = objectMapper.writeValueAsString(event);
                outboxRepository.save(OutboxEvent.fromDomainEvent(UUID.randomUUID().toString(), event, eventPayload));
            } catch (Exception e) {
                logger.error("Failed to serialize event for outbox", e);
                throw new RuntimeException("Failed to save event to outbox", e);
            }
        }

        // Mark events as committed
        payment.markEventsAsCommitted();

        logger.info("Payment processed successfully for order: {}", command.orderId());
    }

    @Override
    public Class<ProcessPaymentCommand> getCommandType() {
        return ProcessPaymentCommand.class;
    }
}
