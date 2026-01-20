package com.konasl.cart.application.port;

import com.konasl.common.outbox.OutboxRepository;

/**
 * Port for storing outbox messages in cart context.
 */
public interface CartOutboxRepository extends OutboxRepository {
}
