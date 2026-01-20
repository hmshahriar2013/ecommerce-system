package com.konasl.useraccess.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to create a new user.
 */
public record CreateUserCommand(
        String userId,
        String email,
        String fullName,
        String role) implements Command {

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}
