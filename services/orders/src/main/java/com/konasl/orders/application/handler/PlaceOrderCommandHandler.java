package com.konasl.orders.application.handler;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.orders.application.command.PlaceOrderCommand;
import com.konasl.orders.application.port.OrderEventStore;
import com.konasl.orders.application.port.OrdersOutboxRepository;
import com.konasl.orders.domain.Order;
import com.konasl.orders.domain.OrderId;
import com.konasl.orders.domain.OrderItem;
import com.konasl.orders.domain.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles placing an order.
 */
@Service
public class PlaceOrderCommandHandler implements CommandHandler<PlaceOrderCommand> {

    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderCommandHandler.class);

    private final OrderEventStore eventStore;
    private final OrdersOutboxRepository outboxRepository;

    public PlaceOrderCommandHandler(
            OrderEventStore eventStore,
            OrdersOutboxRepository outboxRepository) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void handle(PlaceOrderCommand command) {
        logger.info("Placing order for user: {} with {} items",
                command.userId(), command.items().size());

        // Convert DTOs to domain OrderItems
        List<OrderItem> orderItems = command.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.quantity(),
                        "0.00" // Price will be enriched from pricing service
                ))
                .toList();

        // Create Order aggregate
        Order order = new Order(
                OrderId.generate(),
                UserId.of(command.userId()),
                orderItems,
                "0.00", // Total will be calculated
                "user-" + command.userId());

        // Save to event store
        eventStore.save(order);

        // Save to outbox
        for (DomainEvent event : order.getUncommittedEvents()) {
            String eventId = java.util.UUID.randomUUID().toString();
            String eventPayload = serializeEvent(event);
            outboxRepository.save(OutboxEvent.fromDomainEvent(eventId, event, eventPayload));
        }

        // Mark events as committed
        order.markEventsAsCommitted();

        logger.info("Order placed successfully: {}", order.getOrderId().value());
    }

    @Override
    public Class<PlaceOrderCommand> getCommandType() {
        return PlaceOrderCommand.class;
    }

    private String serializeEvent(DomainEvent event) {
        // Simple JSON serialization - in production use Jackson or similar
        return "{\"eventType\":\"" + event.getClass().getSimpleName() + "\"}";
    }
}
