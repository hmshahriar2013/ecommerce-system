package com.konasl.useraccess.adapters.outbound.persistence;

import com.konasl.useraccess.application.port.UserReadRepository;
import com.konasl.useraccess.application.query.UserDto;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of UserReadRepository.
 */
@Repository
public class InMemoryUserReadRepository implements UserReadRepository {

    private final Map<String, UserDto> users = new ConcurrentHashMap<>();
    private final Map<String, String> usernameToUserId = new ConcurrentHashMap<>();

    @Override
    public Optional<UserDto> findById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        String userId = usernameToUserId.get(username);
        if (userId == null) {
            return Optional.empty();
        }
        return findById(userId);
    }

    @Override
    public void save(UserDto user) {
        users.put(user.userId(), user);
        usernameToUserId.put(user.email(), user.userId());
    }
}
