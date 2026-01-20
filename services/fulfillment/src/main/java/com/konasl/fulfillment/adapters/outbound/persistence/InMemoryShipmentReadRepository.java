package com.konasl.fulfillment.adapters.outbound.persistence;

import com.konasl.fulfillment.application.port.ShipmentReadRepository;
import com.konasl.fulfillment.application.query.ShipmentDto;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ShipmentReadRepository.
 */
@Repository
public class InMemoryShipmentReadRepository implements ShipmentReadRepository {

    private final Map<String, ShipmentDto> shipments = new ConcurrentHashMap<>();
    private final Map<String, String> orderIdToShipmentId = new ConcurrentHashMap<>();

    @Override
    public Optional<ShipmentDto> findById(String shipmentId) {
        return Optional.ofNullable(shipments.get(shipmentId));
    }

    @Override
    public Optional<ShipmentDto> findByOrderId(String orderId) {
        String shipmentId = orderIdToShipmentId.get(orderId);
        if (shipmentId == null) {
            return Optional.empty();
        }
        return findById(shipmentId);
    }

    @Override
    public void save(ShipmentDto shipment) {
        shipments.put(shipment.shipmentId(), shipment);
        orderIdToShipmentId.put(shipment.orderId(), shipment.shipmentId());
    }
}
