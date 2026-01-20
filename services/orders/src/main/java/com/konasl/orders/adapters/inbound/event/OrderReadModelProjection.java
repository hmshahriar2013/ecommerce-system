package com.konasl.orders.adapters.inbound.event;

import com.konasl.orders.application.port.OrderReadRepository;
import com.konasl.orders.application.query.OrderDto;
import com.konasl.orders.domain.OrderConfirmedEvent;
import com.konasl.orders.domain.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Projects order domain events to read model.
 */
@Component
public class OrderReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(OrderReadModelProjection.class);

    private final OrderReadRepository repository;

    public OrderReadModelProjection(OrderReadRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        logger.info("Projecting OrderPlacedEvent: {}", event.aggregateId());

        var items = event.items().stream()
                .map(item -> new OrderDto.OrderItemDto(
                        item.productId(),
                        item.quantity(),
                        item.priceAtOrder()))
                .collect(Collectors.toList());

        OrderDto dto = new OrderDto(
                event.aggregateId(),
                event.userId(),
                items,
                event.totalAmount(),
                "PENDING_PAYMENT");

        repository.save(dto);
    }

    @EventListener
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        logger.info("Projecting OrderConfirmedEvent: {}", event.aggregateId());

        repository.findById(event.aggregateId()).ifPresent(existing -> {
            OrderDto updated = new OrderDto(
                    existing.orderId(),
                    existing.userId(),
                    existing.items(),
                    existing.totalAmount(),
                    "CONFIRMED");
            repository.save(updated);
        });
    }
}
