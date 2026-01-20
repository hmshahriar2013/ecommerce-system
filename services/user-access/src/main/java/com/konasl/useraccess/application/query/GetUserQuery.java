package com.konasl.useraccess.application.query;

import com.konasl.common.cqrs.Query;

import java.util.UUID;

/**
 * Query to get user by ID.
 */
public record GetUserQuery(String userId) implements Query<UserDto> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
