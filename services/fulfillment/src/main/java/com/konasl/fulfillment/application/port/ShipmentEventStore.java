package com.konasl.fulfillment.application.port;

import com.konasl.common.eventsourcing.EventStore;
import com.konasl.fulfillment.domain.Shipment;

/**
 * Port for storing and retrieving shipment events.
 */
public interface ShipmentEventStore extends EventStore<Shipment> {
}
