package com.konasl.fulfillment.adapters.inbound.rest;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.cqrs.QueryHandler;
import com.konasl.fulfillment.application.command.CreateShipmentCommand;
import com.konasl.fulfillment.application.command.DispatchShipmentCommand;
import com.konasl.fulfillment.application.query.GetShipmentQuery;
import com.konasl.fulfillment.application.query.ShipmentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for fulfillment operations.
 */
@RestController
@RequestMapping("/api/fulfillment")
public class FulfillmentController {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentController.class);

    private final CommandHandler<CreateShipmentCommand> createShipmentHandler;
    private final CommandHandler<DispatchShipmentCommand> dispatchShipmentHandler;
    private final QueryHandler<GetShipmentQuery, ShipmentDto> getShipmentHandler;

    public FulfillmentController(
            CommandHandler<CreateShipmentCommand> createShipmentHandler,
            CommandHandler<DispatchShipmentCommand> dispatchShipmentHandler,
            QueryHandler<GetShipmentQuery, ShipmentDto> getShipmentHandler) {
        this.createShipmentHandler = createShipmentHandler;
        this.dispatchShipmentHandler = dispatchShipmentHandler;
        this.getShipmentHandler = getShipmentHandler;
    }

    @PostMapping("/shipments")
    public ResponseEntity<ShipmentResponse> createShipment(@RequestBody CreateShipmentRequest request) {
        logger.info("Creating shipment for order: {}", request.orderId());

        var command = new CreateShipmentCommand(request.orderId());
        createShipmentHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ShipmentResponse(
                        command.getCommandId(),
                        "TBD",
                        "READY_TO_SHIP",
                        "Shipment created"));
    }

    @PostMapping("/shipments/{shipmentId}/dispatch")
    public ResponseEntity<ShipmentResponse> dispatchShipment(
            @PathVariable String shipmentId,
            @RequestBody DispatchShipmentRequest request) {

        logger.info("Dispatching shipment: {}", shipmentId);

        var command = new DispatchShipmentCommand(
                shipmentId,
                request.carrier(),
                request.trackingNumber());

        dispatchShipmentHandler.handle(command);

        return ResponseEntity.ok(new ShipmentResponse(
                shipmentId,
                request.trackingNumber(),
                "SHIPPED",
                "Shipment dispatched"));
    }

    @GetMapping("/shipments/{shipmentId}")
    public ResponseEntity<ShipmentDetailResponse> getShipment(@PathVariable String shipmentId) {
        logger.debug("Getting shipment: {}", shipmentId);

        var query = new GetShipmentQuery(shipmentId);
        ShipmentDto shipment = getShipmentHandler.handle(query);

        return ResponseEntity.ok(new ShipmentDetailResponse(
                shipment.shipmentId(),
                shipment.orderId(),
                shipment.trackingNumber(),
                shipment.status()));
    }

    public record CreateShipmentRequest(
            String orderId) {
    }

    public record DispatchShipmentRequest(
            String carrier,
            String trackingNumber) {
    }

    public record ShipmentResponse(
            String shipmentId,
            String trackingNumber,
            String status,
            String message) {
    }

    public record ShipmentDetailResponse(
            String shipmentId,
            String orderId,
            String trackingNumber,
            String status) {
    }
}
