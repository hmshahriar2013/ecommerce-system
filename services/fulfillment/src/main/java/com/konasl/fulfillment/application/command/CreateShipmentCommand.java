package com.konasl.fulfillment.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to create shipment for an order.
 */
public record CreateShipmentCommand(
        String orderId) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
