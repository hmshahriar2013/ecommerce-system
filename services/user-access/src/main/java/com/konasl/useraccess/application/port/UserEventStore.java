package com.konasl.useraccess.application.port;

import com.konasl.common.eventsourcing.EventStore;
import com.konasl.useraccess.domain.User;

/**
 * Port for storing and retrieving user events.
 */
public interface UserEventStore extends EventStore<User> {
}
