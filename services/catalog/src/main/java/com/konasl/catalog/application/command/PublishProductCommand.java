package com.konasl.catalog.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to publish a product, making it visible to customers.
 */
public record PublishProductCommand(
        String productId,
        String publishedBy) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
