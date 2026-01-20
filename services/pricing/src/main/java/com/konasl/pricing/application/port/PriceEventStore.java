package com.konasl.pricing.application.port;

import com.konasl.common.eventsourcing.EventStore;
import com.konasl.pricing.domain.Price;

/**
 * Port for storing and retrieving price events.
 */
public interface PriceEventStore extends EventStore<Price> {
}
