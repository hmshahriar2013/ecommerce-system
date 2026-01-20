package com.konasl.orders.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to confirm an order after successful payment.
 */
public record ConfirmOrderCommand(
        String orderId) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
