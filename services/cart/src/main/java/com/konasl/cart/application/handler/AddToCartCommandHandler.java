package com.konasl.cart.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.cart.application.command.AddToCartCommand;
import com.konasl.cart.application.port.CartEventStore;
import com.konasl.cart.application.port.CartOutboxRepository;
import com.konasl.cart.domain.Cart;
import com.konasl.cart.domain.CartId;
import com.konasl.cart.domain.Money;
import com.konasl.cart.domain.ProductId;
import com.konasl.cart.domain.UserId;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for AddToCartCommand.
 */
@Component
public class AddToCartCommandHandler implements CommandHandler<AddToCartCommand> {

    private static final Logger logger = LoggerFactory.getLogger(AddToCartCommandHandler.class);

    private final CartEventStore eventStore;
    private final CartOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public AddToCartCommandHandler(
            CartEventStore eventStore,
            CartOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(AddToCartCommand command) {
        logger.info("Adding product {} to cart for user {}", command.productId(), command.userId());

        // Try to load existing cart or create new one
        Cart cart;
        try {
            cart = eventStore.load(command.cartId());
        } catch (RuntimeException e) {
            // Cart doesn't exist, create new one
            cart = new Cart(
                    CartId.of(command.cartId()),
                    UserId.of(command.userId()),
                    "system");
        }

        // Add item to cart
        cart.addItem(
                ProductId.of(command.productId()),
                command.quantity(),
                Money.of(command.price(), command.currency()),
                "system");

        // Save events
        eventStore.save(cart);

        // Save each event to outbox wrapped in OutboxEvent
        cart.getUncommittedEvents().forEach(event -> {
            try {
                String eventPayload = objectMapper.writeValueAsString(event);
                OutboxEvent outboxEvent = OutboxEvent.fromDomainEvent(
                        UUID.randomUUID().toString(),
                        event,
                        eventPayload);
                outboxRepository.save(outboxEvent);
            } catch (Exception e) {
                logger.error("Failed to serialize event for outbox", e);
                throw new RuntimeException("Failed to save event to outbox", e);
            }
        });

        cart.markEventsAsCommitted();

        logger.info("Item added to cart: {}", command.cartId());
    }

    @Override
    public Class<AddToCartCommand> getCommandType() {
        return AddToCartCommand.class;
    }
}
