package com.konasl.catalog.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to unpublish a product, hiding it from customers.
 */
public record UnpublishProductCommand(
        String productId,
        String unpublishedBy) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
