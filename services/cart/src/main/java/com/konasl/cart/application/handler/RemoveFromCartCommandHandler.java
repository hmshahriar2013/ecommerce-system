package com.konasl.cart.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.cart.application.command.RemoveFromCartCommand;
import com.konasl.cart.application.port.CartEventStore;
import com.konasl.cart.application.port.CartOutboxRepository;
import com.konasl.cart.domain.Cart;
import com.konasl.cart.domain.ProductId;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for RemoveFromCartCommand.
 */
@Component
public class RemoveFromCartCommandHandler implements CommandHandler<RemoveFromCartCommand> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveFromCartCommandHandler.class);

    private final CartEventStore eventStore;
    private final CartOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public RemoveFromCartCommandHandler(
            CartEventStore eventStore,
            CartOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(RemoveFromCartCommand command) {
        logger.info("Removing product {} from cart {}", command.getProductId(), command.getCartId());

        // Load cart from events
        Cart cart = eventStore.load(command.getCartId());

        // Remove item
        cart.removeItem(ProductId.of(command.getProductId()), "system");

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

        logger.info("Item removed from cart: {}", command.getCartId());
    }

    @Override
    public Class<RemoveFromCartCommand> getCommandType() {
        return RemoveFromCartCommand.class;
    }
}
