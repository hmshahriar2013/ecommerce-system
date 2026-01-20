package com.konasl.inventory.application.port;

import com.konasl.common.eventsourcing.EventStore;
import com.konasl.inventory.domain.Stock;

/**
 * Event store port for Stock aggregate.
 */
public interface StockEventStore extends EventStore<Stock> {
}
