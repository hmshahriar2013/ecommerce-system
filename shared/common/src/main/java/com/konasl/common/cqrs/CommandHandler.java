package com.konasl.common.cqrs;

/**
 * Handler for executing commands.
 * Each command type should have exactly one handler.
 * 
 * CQRS PATTERN:
 * - Handlers contain application logic and orchestration
 * - Handlers coordinate domain objects and persistence
 * - Handlers should be transactional
 * - Handlers validate commands before execution
 * 
 * HEXAGONAL ARCHITECTURE:
 * - Handlers belong to the application layer
 * - Handlers depend on domain entities and port interfaces
 * - Handlers should NOT have framework dependencies
 * 
 * @param <C> The command type this handler processes
 */
public interface CommandHandler<C extends Command> {

    /**
     * Executes the command.
     * 
     * @param command The command to execute
     * @throws IllegalArgumentException                 if command validation fails
     * @throws com.konasl.common.domain.DomainException if business rules are
     *                                                  violated
     */
    void handle(C command);

    /**
     * Returns the command type this handler can process.
     * Used for command routing and registration.
     */
    Class<C> getCommandType();
}
