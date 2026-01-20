package com.konasl.pricing.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.pricing.application.command.SetPriceCommand;
import com.konasl.pricing.application.port.PriceEventStore;
import com.konasl.pricing.application.port.PricingOutboxRepository;
import com.konasl.pricing.domain.Money;
import com.konasl.pricing.domain.Price;
import com.konasl.pricing.domain.ProductId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for SetPriceCommand.
 */
@Component
public class SetPriceCommandHandler implements CommandHandler<SetPriceCommand> {

    private static final Logger logger = LoggerFactory.getLogger(SetPriceCommandHandler.class);

    private final PriceEventStore eventStore;
    private final PricingOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public SetPriceCommandHandler(
            PriceEventStore eventStore,
            PricingOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(SetPriceCommand command) {
        logger.info("Setting price for product {}: {} {}",
                command.productId(), command.amount(), command.currency());

        // Create new price
        Price price = new Price(
                ProductId.of(command.productId()),
                Money.of(command.amount(), command.currency()),
                "system");

        // Save events
        List<DomainEvent> events = price.getUncommittedEvents();
        eventStore.save(price);

        // Save each event to outbox wrapped in OutboxEvent
        events.forEach(event -> {
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

        price.markEventsAsCommitted();

        logger.info("Price set for product: {}", command.productId());
    }

    @Override
    public Class<SetPriceCommand> getCommandType() {
        return SetPriceCommand.class;
    }
}
