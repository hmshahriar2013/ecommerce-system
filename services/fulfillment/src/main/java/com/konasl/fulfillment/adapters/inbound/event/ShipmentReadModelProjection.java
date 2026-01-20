package com.konasl.fulfillment.adapters.inbound.event;

import com.konasl.fulfillment.application.query.ShipmentDto;
import com.konasl.fulfillment.application.port.ShipmentReadRepository;
import com.konasl.fulfillment.domain.ShipmentCreatedEvent;
import com.konasl.fulfillment.domain.ShipmentDispatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Projection that updates the shipment read model based on domain events.
 */
@Component
public class ShipmentReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentReadModelProjection.class);

    private final ShipmentReadRepository readRepository;

    public ShipmentReadModelProjection(ShipmentReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @EventListener
    public void on(ShipmentCreatedEvent event) {
        logger.info("Projecting ShipmentCreatedEvent: {}", event.aggregateId());

        ShipmentDto shipment = new ShipmentDto(
                event.aggregateId(),
                event.orderId(),
                "READY_TO_SHIP",
                null,
                event.trackingNumber(),
                null,
                null,
                null,
                null);

        readRepository.save(shipment);
    }

    @EventListener
    public void on(ShipmentDispatchedEvent event) {
        logger.info("Projecting ShipmentDispatchedEvent: {}", event.aggregateId());

        readRepository.findById(event.aggregateId()).ifPresent(existing -> {
            ShipmentDto updated = new ShipmentDto(
                    existing.shipmentId(),
                    existing.orderId(),
                    "SHIPPED",
                    existing.carrier(),
                    existing.trackingNumber(),
                    existing.streetAddress(),
                    existing.city(),
                    existing.postalCode(),
                    existing.country());
            readRepository.save(updated);
        });
    }
}
