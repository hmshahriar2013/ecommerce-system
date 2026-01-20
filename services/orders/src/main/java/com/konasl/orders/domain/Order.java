package com.konasl.orders.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order aggregate root in the Orders bounded context.
 * 
 * BUSINESS RULES:
 * - Orders must have at least one item
 * - Orders start with PENDING_PAYMENT status
 * - Only pending orders can be confirmed
 * - Confirmed orders trigger inventory confirmation
 * - Cancelled orders release inventory reservations
 */
public class Order extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    private OrderId orderId;
    private UserId userId;
    private List<OrderItem> items = new ArrayList<>();
    private String totalAmount;
    private OrderStatus status;

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public Order() {
        super();
    }

    /**
     * Places a new order (command method).
     */
    public Order(OrderId orderId, UserId userId, List<OrderItem> items, String totalAmount, String placedBy) {
        super();

        validateOrder(items);

        raiseEvent(new OrderPlacedEvent(
                UUID.randomUUID().toString(),
                orderId.value(),
                userId.value(),
                List.copyOf(items),
                totalAmount,
                Instant.now(),
                placedBy));

        logger.info("Order placed: {} by user {} with {} items", orderId.value(), userId.value(), items.size());
    }

    /**
     * Confirms order after successful payment.
     */
    public void confirm(String confirmedBy) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Can only confirm orders in PENDING_PAYMENT status");
        }

        raiseEvent(new OrderConfirmedEvent(
                UUID.randomUUID().toString(),
                orderId.value(),
                orderId.value(),
                Instant.now(),
                confirmedBy));

        logger.info("Order confirmed: {}", orderId.value());
    }

    /**
     * Applies events to rebuild aggregate state.
     */
    @Override
    protected void apply(DomainEvent event) {
        switch (event) {
            case OrderPlacedEvent e -> applyOrderPlaced(e);
            case OrderConfirmedEvent e -> applyOrderConfirmed(e);
            default -> logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyOrderPlaced(OrderPlacedEvent event) {
        this.orderId = OrderId.of(event.aggregateId());
        this.userId = UserId.of(event.userId());
        this.items = new ArrayList<>(event.items());
        this.totalAmount = event.totalAmount();
        this.status = OrderStatus.PENDING_PAYMENT;
        setAggregateId(event.aggregateId());
    }

    private void applyOrderConfirmed(OrderConfirmedEvent event) {
        this.status = OrderStatus.CONFIRMED;
    }

    private void validateOrder(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }

    // Getters

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
