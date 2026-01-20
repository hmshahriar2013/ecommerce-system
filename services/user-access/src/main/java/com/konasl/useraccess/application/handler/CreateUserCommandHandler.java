package com.konasl.useraccess.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.useraccess.application.command.CreateUserCommand;
import com.konasl.useraccess.application.port.UserAccessOutboxRepository;
import com.konasl.useraccess.application.port.UserEventStore;
import com.konasl.useraccess.domain.Email;
import com.konasl.useraccess.domain.User;
import com.konasl.useraccess.domain.UserId;
import com.konasl.useraccess.domain.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for CreateUserCommand.
 */
@Component
public class CreateUserCommandHandler implements CommandHandler<CreateUserCommand> {

    private static final Logger logger = LoggerFactory.getLogger(CreateUserCommandHandler.class);

    private final UserEventStore eventStore;
    private final UserAccessOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public CreateUserCommandHandler(
            UserEventStore eventStore,
            UserAccessOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(CreateUserCommand command) {
        logger.info("Creating user: {}", command.email());

        // Create new user
        User user = new User(
                UserId.of(UUID.randomUUID().toString()),
                command.email(),
                command.fullName(),
                com.konasl.useraccess.domain.UserRole.valueOf(command.role()),
                "system");

        // Save events
        eventStore.save(user);

        // Save each event to outbox wrapped in OutboxEvent
        List<DomainEvent> events = user.getUncommittedEvents();
        events.forEach(event -> {
            try {
                String eventPayload = objectMapper.writeValueAsString(event);
                OutboxEvent outboxEvent = OutboxEvent.fromDomainEvent(
                        UUID.randomUUID().toString(),
                        event,
                        eventPayload);
                outboxRepository.save(outboxEvent);
            } catch (Exception e) {
                logger.error("Failed to serialize event for outbox", e);
                throw new RuntimeException("Failed to save event to outbox", e);
            }
        });

        user.markEventsAsCommitted();

        logger.info("User created: {}", user.getUserId().value());
    }

    @Override
    public Class<CreateUserCommand> getCommandType() {
        return CreateUserCommand.class;
    }
}
