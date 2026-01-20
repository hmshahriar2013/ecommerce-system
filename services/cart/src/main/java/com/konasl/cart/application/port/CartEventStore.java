package com.konasl.cart.application.port;

import com.konasl.common.eventsourcing.EventStore;
import com.konasl.cart.domain.Cart;

/**
 * Port for storing and retrieving cart events.
 */
public interface CartEventStore extends EventStore<Cart> {
}
