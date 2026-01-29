package com.konasl.fulfillment.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Shipment aggregate root in the Fulfillment bounded context.
 * 
 * BUSINESS RULES:
 * - Shipments are created when orders are confirmed
 * - Each shipment has a unique tracking number
 * - Shipments start with READY_TO_SHIP status
 * - Only ready shipments can be dispatched
 */
public class Shipment extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(Shipment.class);

    private ShipmentId shipmentId;
    private OrderId orderId;
    private String trackingNumber;
    private ShipmentStatus status;

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public Shipment() {
        super();
    }

    /**
     * Creates a new shipment (command method).
     */
    public Shipment(ShipmentId shipmentId, OrderId orderId, String createdBy) {
        super();

        String trackingNumber = generateTrackingNumber();

        raiseEvent(new ShipmentCreatedEvent(
                UUID.randomUUID().toString(),
                shipmentId.value(),
                orderId.value(),
                trackingNumber,
                Instant.now(),
                createdBy));

        logger.info("Shipment created for order {}: tracking {}", orderId.value(), trackingNumber);
    }

    /**
     * Dispatches the shipment (changes status to SHIPPED).
     */
    public void dispatch(String carrier, String trackingNumber, String dispatchedBy) {
        if (status != ShipmentStatus.READY_TO_SHIP) {
            throw new IllegalStateException("Can only dispatch shipments in READY_TO_SHIP status");
        }

        // Update tracking number
        this.trackingNumber = trackingNumber;

        raiseEvent(new ShipmentDispatchedEvent(
                UUID.randomUUID().toString(),
                shipmentId.value(),
                trackingNumber,
                Instant.now(),
                dispatchedBy));

        logger.info("Shipment dispatched: {} with tracking: {}, carrier: {}",
                shipmentId.value(), trackingNumber, carrier);
    }

    /**
     * Applies events to rebuild aggregate state.
     */
    @Override
    public void apply(DomainEvent event) {
        switch (event) {
            case ShipmentCreatedEvent e -> applyShipmentCreated(e);
            case ShipmentDispatchedEvent e -> applyShipmentDispatched(e);
            default -> logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyShipmentCreated(ShipmentCreatedEvent event) {
        this.shipmentId = ShipmentId.of(event.aggregateId());
        this.orderId = OrderId.of(event.orderId());
        this.trackingNumber = event.trackingNumber();
        this.status = ShipmentStatus.READY_TO_SHIP;
        setAggregateId(event.aggregateId());
    }

    private void applyShipmentDispatched(ShipmentDispatchedEvent event) {
        this.status = ShipmentStatus.SHIPPED;
    }

    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters

    public ShipmentId getShipmentId() {
        return shipmentId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public ShipmentStatus getStatus() {
        return status;
    }
}
