package com.konasl.payments.application.command;

import com.konasl.common.cqrs.Command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to process payment.
 */
public record ProcessPaymentCommand(
        String orderId,
        BigDecimal amount,
        String currency) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
