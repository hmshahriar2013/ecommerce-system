package com.konasl.orders.application.handler;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.orders.application.command.ConfirmOrderCommand;
import com.konasl.orders.application.port.OrderEventStore;
import com.konasl.orders.application.port.OrdersOutboxRepository;
import com.konasl.orders.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles confirming an order after successful payment.
 */
@Service
public class ConfirmOrderCommandHandler implements CommandHandler<ConfirmOrderCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmOrderCommandHandler.class);

    private final OrderEventStore eventStore;
    private final OrdersOutboxRepository outboxRepository;

    public ConfirmOrderCommandHandler(
            OrderEventStore eventStore,
            OrdersOutboxRepository outboxRepository) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void handle(ConfirmOrderCommand command) {
        logger.info("Confirming order: {}", command.orderId());

        // Load Order aggregate
        Order order = eventStore.load(command.orderId());

        // Confirm order
        order.confirm("payment-service");

        // Save new events
        eventStore.save(order);

        // Save to outbox
        for (DomainEvent event : order.getUncommittedEvents()) {
            String eventId = java.util.UUID.randomUUID().toString();
            String eventPayload = serializeEvent(event);
            outboxRepository.save(OutboxEvent.fromDomainEvent(eventId, event, eventPayload));
        }

        // Mark events as committed
        order.markEventsAsCommitted();

        logger.info("Order confirmed successfully: {}", command.orderId());
    }

    @Override
    public Class<ConfirmOrderCommand> getCommandType() {
        return ConfirmOrderCommand.class;
    }

    private String serializeEvent(DomainEvent event) {
        // Simple JSON serialization - in production use Jackson or similar
        return "{\"eventType\":\"" + event.getClass().getSimpleName() + "\"}";
    }
}
