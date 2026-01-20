package com.konasl.cart.adapters.inbound.event;

import com.konasl.cart.application.port.CartReadRepository;
import com.konasl.cart.application.query.CartDto;
import com.konasl.cart.domain.CartCreatedEvent;
import com.konasl.cart.domain.ItemAddedToCartEvent;
import com.konasl.cart.domain.ItemRemovedFromCartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Projection that updates the cart read model based on domain events.
 */
@Component
public class CartReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(CartReadModelProjection.class);

    private final CartReadRepository readRepository;

    public CartReadModelProjection(CartReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @EventListener
    public void on(CartCreatedEvent event) {
        logger.info("Projecting CartCreatedEvent: {}", event.aggregateId());

        CartDto cart = new CartDto(
                event.aggregateId(),
                event.userId(),
                new ArrayList<>(),
                BigDecimal.ZERO);

        readRepository.save(cart);
    }

    @EventListener
    public void on(ItemAddedToCartEvent event) {
        logger.info("Projecting ItemAddedToCartEvent: product {} to cart {}",
                event.productId(), event.aggregateId());

        readRepository.findById(event.aggregateId()).ifPresent(existing -> {
            List<CartDto.CartItemDto> updatedItems = new ArrayList<>(existing.items());

            // Check if product already in cart
            boolean found = false;
            for (int i = 0; i < updatedItems.size(); i++) {
                CartDto.CartItemDto item = updatedItems.get(i);
                if (item.productId().equals(event.productId())) {
                    // Update quantity
                    updatedItems.set(i, new CartDto.CartItemDto(
                            item.productId(),
                            item.quantity() + event.quantity(),
                            item.price()));
                    found = true;
                    break;
                }
            }

            if (!found) {
                updatedItems.add(new CartDto.CartItemDto(
                        event.productId(),
                        event.quantity(),
                        event.price()));
            }

            BigDecimal total = updatedItems.stream()
                    .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CartDto updated = new CartDto(
                    existing.cartId(),
                    existing.userId(),
                    updatedItems,
                    total);

            readRepository.save(updated);
        });
    }

    @EventListener
    public void on(ItemRemovedFromCartEvent event) {
        logger.info("Projecting ItemRemovedFromCartEvent: product {} from cart {}",
                event.productId(), event.aggregateId());

        readRepository.findById(event.aggregateId()).ifPresent(existing -> {
            List<CartDto.CartItemDto> updatedItems = existing.items().stream()
                    .filter(item -> !item.productId().equals(event.productId()))
                    .toList();

            BigDecimal total = updatedItems.stream()
                    .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CartDto updated = new CartDto(
                    existing.cartId(),
                    existing.userId(),
                    updatedItems,
                    total);

            readRepository.save(updated);
        });
    }
}
