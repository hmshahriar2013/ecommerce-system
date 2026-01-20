package com.konasl.useraccess.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * User aggregate root in the User Access bounded context.
 * 
 * BUSINESS RULES:
 * - Email must be unique (checked before creation)
 * - Users start with ACTIVE status
 * - Users have a single role (CUSTOMER or ADMIN)
 */
public class User extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    private UserId userId;
    private String email;
    private String fullName;
    private UserRole role;
    private UserStatus status;

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public User() {
        super();
    }

    /**
     * Creates a new user (command method).
     */
    public User(UserId userId, String email, String fullName, UserRole role, String createdBy) {
        super();

        validateUserCreation(email, fullName);

        raiseEvent(new UserCreatedEvent(
                UUID.randomUUID().toString(),
                userId.value(),
                email,
                fullName,
                role.name(),
                Instant.now(),
                createdBy));

        logger.info("User created: {} with role: {}", email, role);
    }

    /**
     * Applies events to rebuild aggregate state.
     */
    @Override
    public void apply(DomainEvent event) {
        if (event instanceof UserCreatedEvent e) {
            applyUserCreated(e);
        } else {
            logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyUserCreated(UserCreatedEvent event) {
        this.userId = UserId.of(event.aggregateId());
        this.email = event.email();
        this.fullName = event.fullName();
        this.role = UserRole.valueOf(event.role());
        this.status = UserStatus.ACTIVE;
        setAggregateId(event.aggregateId());
    }

    private void validateUserCreation(String email, String fullName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
    }

    // Getters

    public UserId getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
