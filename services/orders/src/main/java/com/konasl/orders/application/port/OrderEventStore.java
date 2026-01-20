package com.konasl.orders.application.port;

import com.konasl.common.eventsourcing.EventStore;
import com.konasl.orders.domain.Order;

/**
 * Event store port for Order aggregate.
 */
public interface OrderEventStore extends EventStore<Order> {
}
