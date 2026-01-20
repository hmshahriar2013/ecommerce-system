package com.konasl.useraccess.adapters.inbound.event;

import com.konasl.useraccess.application.port.UserReadRepository;
import com.konasl.useraccess.application.query.UserDto;
import com.konasl.useraccess.domain.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Projection that updates the user read model based on domain events.
 */
@Component
public class UserReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(UserReadModelProjection.class);

    private final UserReadRepository readRepository;

    public UserReadModelProjection(UserReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @EventListener
    public void on(UserCreatedEvent event) {
        logger.info("Projecting UserCreatedEvent: {}", event.aggregateId());

        UserDto user = new UserDto(
                event.aggregateId(),
                event.email(),
                event.fullName(),
                event.role(),
                "ACTIVE");

        readRepository.save(user);
    }
}
