package com.konasl.pricing.application.port;

import com.konasl.common.outbox.OutboxRepository;

/**
 * Port for storing outbox messages in pricing context.
 */
public interface PricingOutboxRepository extends OutboxRepository {
}
