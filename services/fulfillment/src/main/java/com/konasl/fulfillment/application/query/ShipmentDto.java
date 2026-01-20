package com.konasl.fulfillment.application.query;

/**
 * DTO representing shipment details for query results.
 */
public record ShipmentDto(
        String shipmentId,
        String orderId,
        String status,
        String carrier,
        String trackingNumber,
        String streetAddress,
        String city,
        String postalCode,
        String country) {
}
