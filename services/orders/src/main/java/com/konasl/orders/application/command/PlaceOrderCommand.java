package com.konasl.orders.application.command;

import com.konasl.common.cqrs.Command;

import java.util.List;
import java.util.UUID;

/**
 * Command to place an order.
 */
public record PlaceOrderCommand(
        String userId,
        List<OrderItemDto> items) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }

    public record OrderItemDto(
            String productId,
            int quantity) {
    }
}
