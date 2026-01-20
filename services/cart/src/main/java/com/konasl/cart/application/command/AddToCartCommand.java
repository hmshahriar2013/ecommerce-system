package com.konasl.cart.application.command;

import com.konasl.common.cqrs.Command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to add item to cart.
 */
public record AddToCartCommand(
        String cartId,
        String userId,
        String productId,
        int quantity,
        BigDecimal price,
        String currency) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
