package com.konasl.fulfillment.application.port;

import com.konasl.common.outbox.OutboxRepository;

/**
 * Port for storing outbox messages in fulfillment context.
 */
public interface FulfillmentOutboxRepository extends OutboxRepository {
}
