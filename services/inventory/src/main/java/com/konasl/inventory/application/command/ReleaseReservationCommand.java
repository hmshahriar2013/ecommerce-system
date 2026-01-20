package com.konasl.inventory.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to release a stock reservation.
 */
public record ReleaseReservationCommand(
        String stockId,
        String reservationId) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
