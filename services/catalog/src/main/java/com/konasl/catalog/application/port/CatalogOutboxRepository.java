package com.konasl.catalog.application.port;

import com.konasl.common.outbox.OutboxRepository;

/**
 * Port (interface) for Outbox repository.
 * Implementation will be in the adapter layer.
 */
public interface CatalogOutboxRepository extends OutboxRepository {
}
