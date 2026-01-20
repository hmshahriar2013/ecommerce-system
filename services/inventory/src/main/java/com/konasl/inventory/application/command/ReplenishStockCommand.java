package com.konasl.inventory.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to replenish stock.
 */
public record ReplenishStockCommand(
        String stockId,
        int quantity) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
