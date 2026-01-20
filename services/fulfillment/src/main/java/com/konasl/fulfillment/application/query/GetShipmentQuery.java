package com.konasl.fulfillment.application.query;

import com.konasl.common.cqrs.Query;

import java.util.UUID;

/**
 * Query to retrieve shipment details.
 */
public record GetShipmentQuery(String shipmentId) implements Query<ShipmentDto> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
