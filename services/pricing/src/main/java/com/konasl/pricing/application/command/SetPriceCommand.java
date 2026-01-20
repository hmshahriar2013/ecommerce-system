package com.konasl.pricing.application.command;

import com.konasl.common.cqrs.Command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to set price for a product.
 */
public record SetPriceCommand(
        String productId,
        BigDecimal amount,
        String currency) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
