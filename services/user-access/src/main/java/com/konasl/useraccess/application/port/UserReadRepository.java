package com.konasl.useraccess.application.port;

import com.konasl.useraccess.application.query.UserDto;

import java.util.Optional;

/**
 * Port for read-side user repository.
 */
public interface UserReadRepository {

    Optional<UserDto> findById(String userId);

    Optional<UserDto> findByUsername(String username);

    void save(UserDto user);
}
