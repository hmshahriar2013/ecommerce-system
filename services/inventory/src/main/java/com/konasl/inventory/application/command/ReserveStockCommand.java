package com.konasl.inventory.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to reserve stock for an order.
 */
public record ReserveStockCommand(
        String stockId,
        String productId,
        String reservationId,
        int quantity) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
