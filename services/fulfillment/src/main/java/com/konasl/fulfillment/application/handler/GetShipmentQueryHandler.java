package com.konasl.fulfillment.application.handler;

import com.konasl.common.cqrs.QueryHandler;
import com.konasl.fulfillment.application.port.ShipmentReadRepository;
import com.konasl.fulfillment.application.query.GetShipmentQuery;
import com.konasl.fulfillment.application.query.ShipmentDto;
import org.springframework.stereotype.Component;

/**
 * Handler for GetShipmentQuery.
 */
@Component
public class GetShipmentQueryHandler implements QueryHandler<GetShipmentQuery, ShipmentDto> {

    private final ShipmentReadRepository readRepository;

    public GetShipmentQueryHandler(ShipmentReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Override
    public ShipmentDto handle(GetShipmentQuery query) {
        return readRepository.findById(query.shipmentId())
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + query.shipmentId()));
    }

    @Override
    public Class<GetShipmentQuery> getQueryType() {
        return GetShipmentQuery.class;
    }
}
