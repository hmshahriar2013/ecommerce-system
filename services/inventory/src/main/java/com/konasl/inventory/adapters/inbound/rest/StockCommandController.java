package com.konasl.inventory.adapters.inbound.rest;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.inventory.application.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for stock command operations.
 */
@RestController
@RequestMapping("/api/inventory/stock")
public class StockCommandController {

    private static final Logger logger = LoggerFactory.getLogger(StockCommandController.class);

    private final CommandHandler<InitializeStockCommand> initializeHandler;
    private final CommandHandler<ReserveStockCommand> reserveHandler;
    private final CommandHandler<ReleaseReservationCommand> releaseHandler;
    private final CommandHandler<ConfirmReservationCommand> confirmHandler;
    private final CommandHandler<ReplenishStockCommand> replenishHandler;

    public StockCommandController(
            CommandHandler<InitializeStockCommand> initializeHandler,
            CommandHandler<ReserveStockCommand> reserveHandler,
            CommandHandler<ReleaseReservationCommand> releaseHandler,
            CommandHandler<ConfirmReservationCommand> confirmHandler,
            CommandHandler<ReplenishStockCommand> replenishHandler) {
        this.initializeHandler = initializeHandler;
        this.reserveHandler = reserveHandler;
        this.releaseHandler = releaseHandler;
        this.confirmHandler = confirmHandler;
        this.replenishHandler = replenishHandler;
    }

    @PostMapping
    public ResponseEntity<StockResponse> initializeStock(@RequestBody InitializeStockRequest request) {
        logger.info("Initializing stock for product: {}", request.productId());

        var command = new InitializeStockCommand(
                request.productId(),
                request.initialQuantity(),
                request.lowStockThreshold());

        initializeHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new StockResponse("Stock initialized successfully"));
    }

    @PostMapping("/{stockId}/reserve")
    public ResponseEntity<StockResponse> reserveStock(
            @PathVariable String stockId,
            @RequestBody ReserveStockRequest request) {
        logger.info("Reserving stock: {} for product: {}", stockId, request.productId());

        var command = new ReserveStockCommand(
                stockId,
                request.productId(),
                request.reservationId(),
                request.quantity());

        reserveHandler.handle(command);

        return ResponseEntity.ok(new StockResponse("Stock reserved successfully"));
    }

    @PostMapping("/{stockId}/release")
    public ResponseEntity<StockResponse> releaseReservation(
            @PathVariable String stockId,
            @RequestBody ReleaseReservationRequest request) {
        logger.info("Releasing reservation: {}", request.reservationId());

        var command = new ReleaseReservationCommand(stockId, request.reservationId());

        releaseHandler.handle(command);

        return ResponseEntity.ok(new StockResponse("Reservation released successfully"));
    }

    @PostMapping("/{stockId}/confirm")
    public ResponseEntity<StockResponse> confirmReservation(
            @PathVariable String stockId,
            @RequestBody ConfirmReservationRequest request) {
        logger.info("Confirming reservation: {}", request.reservationId());

        var command = new ConfirmReservationCommand(stockId, request.reservationId());

        confirmHandler.handle(command);

        return ResponseEntity.ok(new StockResponse("Reservation confirmed successfully"));
    }

    @PostMapping("/{stockId}/replenish")
    public ResponseEntity<StockResponse> replenishStock(
            @PathVariable String stockId,
            @RequestBody ReplenishStockRequest request) {
        logger.info("Replenishing stock: {}", stockId);

        var command = new ReplenishStockCommand(stockId, request.quantity());

        replenishHandler.handle(command);

        return ResponseEntity.ok(new StockResponse("Stock replenished successfully"));
    }

    // Request/Response DTOs

    public record InitializeStockRequest(
            String productId,
            int initialQuantity,
            int lowStockThreshold) {
    }

    public record ReserveStockRequest(
            String productId,
            String reservationId,
            int quantity) {
    }

    public record ReleaseReservationRequest(
            String reservationId) {
    }

    public record ConfirmReservationRequest(
            String reservationId) {
    }

    public record ReplenishStockRequest(
            int quantity) {
    }

    public record StockResponse(
            String message) {
    }
}
