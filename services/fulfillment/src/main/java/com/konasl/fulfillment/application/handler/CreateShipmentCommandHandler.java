package com.konasl.fulfillment.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.fulfillment.application.command.CreateShipmentCommand;
import com.konasl.fulfillment.application.port.FulfillmentOutboxRepository;
import com.konasl.fulfillment.application.port.ShipmentEventStore;
import com.konasl.fulfillment.domain.OrderId;
import com.konasl.fulfillment.domain.Shipment;
import com.konasl.fulfillment.domain.ShipmentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for CreateShipmentCommand.
 */
@Component
public class CreateShipmentCommandHandler implements CommandHandler<CreateShipmentCommand> {

    private static final Logger logger = LoggerFactory.getLogger(CreateShipmentCommandHandler.class);

    private final ShipmentEventStore eventStore;
    private final FulfillmentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public CreateShipmentCommandHandler(
            ShipmentEventStore eventStore,
            FulfillmentOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(CreateShipmentCommand command) {
        logger.info("Creating shipment for order: {}", command.orderId());

        // Use orderId as shipmentId for simplicity (one shipment per order)
        ShipmentId shipmentId = ShipmentId.of(command.orderId());

        // Create new shipment using domain model
        Shipment shipment = new Shipment(
                shipmentId,
                OrderId.of(command.orderId()),
                "system");

        // Save events
        List<DomainEvent> events = shipment.getUncommittedEvents();
        eventStore.save(shipment);

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

        shipment.markEventsAsCommitted();

        logger.info("Shipment created with ID: {} for order: {}", shipmentId.value(), command.orderId());
    }

    @Override
    public Class<CreateShipmentCommand> getCommandType() {
        return CreateShipmentCommand.class;
    }
}
