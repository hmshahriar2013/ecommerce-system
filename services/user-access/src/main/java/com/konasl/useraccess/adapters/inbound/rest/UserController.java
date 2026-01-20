package com.konasl.useraccess.adapters.inbound.rest;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.cqrs.QueryHandler;
import com.konasl.useraccess.application.command.CreateUserCommand;
import com.konasl.useraccess.application.query.GetUserQuery;
import com.konasl.useraccess.application.query.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final CommandHandler<CreateUserCommand> createUserHandler;
    private final QueryHandler<GetUserQuery, UserDto> getUserHandler;

    public UserController(
            CommandHandler<CreateUserCommand> createUserHandler,
            QueryHandler<GetUserQuery, UserDto> getUserHandler) {
        this.createUserHandler = createUserHandler;
        this.getUserHandler = getUserHandler;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        logger.info("Creating user: {}", request.email());

        var command = new CreateUserCommand(
                null, // userId will be generated
                request.email(),
                request.fullName(),
                request.role());

        createUserHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponse(command.getCommandId(), "User created successfully"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUser(@PathVariable String userId) {
        logger.debug("Getting user: {}", userId);

        var query = new GetUserQuery(userId);
        UserDto user = getUserHandler.handle(query);

        return ResponseEntity.ok(new UserDetailResponse(
                user.userId(),
                user.email(),
                user.fullName(),
                user.role(),
                user.status()));
    }

    public record CreateUserRequest(
            String email,
            String fullName,
            String role) {
    }

    public record UserResponse(
            String userId,
            String message) {
    }

    public record UserDetailResponse(
            String userId,
            String email,
            String fullName,
            String role,
            String status) {
    }
}
