package com.konasl.fulfillment.application.port;

import com.konasl.fulfillment.application.query.ShipmentDto;

import java.util.Optional;

/**
 * Port for read-side shipment repository.
 */
public interface ShipmentReadRepository {

    Optional<ShipmentDto> findById(String shipmentId);

    Optional<ShipmentDto> findByOrderId(String orderId);

    void save(ShipmentDto shipment);
}
