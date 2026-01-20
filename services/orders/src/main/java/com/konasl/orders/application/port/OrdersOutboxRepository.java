package com.konasl.orders.application.port;

import com.konasl.common.outbox.OutboxRepository;

/**
 * Outbox repository port for Orders bounded context.
 */
public interface OrdersOutboxRepository extends OutboxRepository {
}
