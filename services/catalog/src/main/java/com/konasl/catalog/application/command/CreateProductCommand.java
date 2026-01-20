package com.konasl.catalog.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to create a new product in the catalog.
 */
public record CreateProductCommand(
        String productId,
        String name,
        String description,
        String category,
        String imageUrl,
        String createdBy) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
