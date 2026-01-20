package com.konasl.fulfillment.application.command;

import com.konasl.common.cqrs.Command;
import lombok.Getter;

import java.util.UUID;

/**
 * Command to dispatch a shipment.
 */
public final class DispatchShipmentCommand implements Command {

    private final String commandId;
    @Getter
    private final String shipmentId;
    private final String carrier;
    private final String trackingNumber;

    public DispatchShipmentCommand(String shipmentId, String carrier, String trackingNumber) {
        this.commandId = UUID.randomUUID().toString();
        this.shipmentId = shipmentId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
