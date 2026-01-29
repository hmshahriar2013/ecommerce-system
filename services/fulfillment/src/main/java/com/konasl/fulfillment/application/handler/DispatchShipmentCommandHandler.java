package com.konasl.fulfillment.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.fulfillment.application.command.DispatchShipmentCommand;
import com.konasl.fulfillment.application.port.FulfillmentOutboxRepository;
import com.konasl.fulfillment.application.port.ShipmentEventStore;
import com.konasl.fulfillment.domain.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for DispatchShipmentCommand.
 */
@Component
public class DispatchShipmentCommandHandler implements CommandHandler<DispatchShipmentCommand> {

    private static final Logger logger = LoggerFactory.getLogger(DispatchShipmentCommandHandler.class);

    private final ShipmentEventStore eventStore;
    private final FulfillmentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public DispatchShipmentCommandHandler(
            ShipmentEventStore eventStore,
            FulfillmentOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(DispatchShipmentCommand command) {
        logger.info("Dispatching shipment: {} with carrier: {}",
                command.getShipmentId(), command.getCarrier());

        // Load shipment from events
        Shipment shipment = eventStore.load(command.getShipmentId());

        // Dispatch shipment with carrier and tracking info
        shipment.dispatch(command.getCarrier(), command.getTrackingNumber(), "system");

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

        logger.info("Shipment dispatched: {} with tracking: {}",
                command.getShipmentId(), command.getTrackingNumber());
    }

    @Override
    public Class<DispatchShipmentCommand> getCommandType() {
        return DispatchShipmentCommand.class;
    }
}
