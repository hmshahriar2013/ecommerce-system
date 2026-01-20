package com.konasl.catalog.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to update product details.
 */
public record UpdateProductCommand(
        String productId,
        String name,
        String description,
        String category,
        String imageUrl,
        String updatedBy) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
