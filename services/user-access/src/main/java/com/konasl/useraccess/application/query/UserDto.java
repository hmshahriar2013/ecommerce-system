package com.konasl.useraccess.application.query;

/**
 * DTO for user read model.
 */
public record UserDto(
        String userId,
        String email,
        String fullName,
        String role,
        String status) {
}
