package com.konasl.useraccess.application.port;

import com.konasl.common.outbox.OutboxRepository;

/**
 * Port for storing outbox messages in user access context.
 */
public interface UserAccessOutboxRepository extends OutboxRepository {
}
