package com.konasl.catalog.application.port;

import com.konasl.catalog.domain.Product;
import com.konasl.common.eventsourcing.EventStore;

/**
 * Port (interface) for Product event store.
 * Implementation will be in the adapter layer.
 */
public interface ProductEventStore extends EventStore<Product> {
}
