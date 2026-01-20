package com.konasl.inventory.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to initialize stock for a product.
 */
public record InitializeStockCommand(
        String productId,
        int initialQuantity,
        int lowStockThreshold) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
