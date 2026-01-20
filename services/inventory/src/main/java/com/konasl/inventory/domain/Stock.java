package com.konasl.inventory.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stock aggregate root in the Inventory bounded context.
 * 
 * BUSINESS RULES:
 * - Cannot reserve more stock than available
 * - Reservations reduce available stock
 * - Released reservations restore available stock
 * - Confirmed reservations permanently reduce stock
 * - Prevents overselling through reservation mechanism
 * 
 * HEXAGONAL ARCHITECTURE:
 * - Pure domain logic with NO framework dependencies
 * - All state changes produce domain events
 */
public class Stock extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(Stock.class);
    private static final int LOW_STOCK_THRESHOLD = 10;

    private StockId stockId;
    private ProductId productId;
    private int totalQuantity;
    private int availableQuantity;
    private Map<ReservationId, Integer> reservations;

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public Stock() {
        super();
        this.reservations = new HashMap<>();
    }

    /**
     * Creates new stock for a product (command method).
     */
    public Stock(StockId stockId, ProductId productId, int initialQuantity, String createdBy) {
        super();
        this.reservations = new HashMap<>();

        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative");
        }

        raiseEvent(new StockInitializedEvent(
                UUID.randomUUID().toString(),
                stockId.value(),
                productId.value(),
                initialQuantity,
                Instant.now(),
                createdBy));

        logger.info("Stock initialized: {} for product: {}", stockId.value(), productId.value());
    }

    /**
     * Reserves stock for an order.
     * 
     * BUSINESS RULE: Cannot reserve more than available quantity (prevents
     * overselling).
     */
    public void reserve(ReservationId reservationId, String orderId, int quantity, String reservedBy) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }

        if (reservations.containsKey(reservationId)) {
            logger.warn("Reservation already exists: {}", reservationId.value());
            throw new IllegalStateException("Reservation already exists: " + reservationId.value());
        }

        if (availableQuantity < quantity) {
            logger.error("Insufficient stock: available={}, requested={}", availableQuantity, quantity);
            throw new InsufficientStockException(
                    "Insufficient stock for product " + productId.value() +
                            ": available=" + availableQuantity + ", requested=" + quantity);
        }

        raiseEvent(new StockReservedEvent(
                UUID.randomUUID().toString(),
                stockId.value(),
                reservationId.value(),
                orderId,
                quantity,
                Instant.now(),
                reservedBy));

        logger.info("Stock reserved: {} units for order: {}", quantity, orderId);
    }

    /**
     * Releases a reservation (order cancelled or payment failed).
     */
    public void releaseReservation(ReservationId reservationId, String releasedBy) {
        if (!reservations.containsKey(reservationId)) {
            logger.warn("Reservation not found: {}", reservationId.value());
            throw new IllegalArgumentException("Reservation not found: " + reservationId.value());
        }

        int quantity = reservations.get(reservationId);

        raiseEvent(new ReservationReleasedEvent(
                UUID.randomUUID().toString(),
                stockId.value(),
                reservationId.value(),
                quantity,
                Instant.now(),
                releasedBy));

        logger.info("Reservation released: {} ({} units)", reservationId.value(), quantity);
    }

    /**
     * Confirms a reservation (payment successful).
     * This permanently reduces stock.
     */
    public void confirmReservation(ReservationId reservationId, String confirmedBy) {
        if (!reservations.containsKey(reservationId)) {
            logger.warn("Reservation not found: {}", reservationId.value());
            throw new IllegalArgumentException("Reservation not found: " + reservationId.value());
        }

        int quantity = reservations.get(reservationId);

        raiseEvent(new ReservationConfirmedEvent(
                UUID.randomUUID().toString(),
                stockId.value(),
                reservationId.value(),
                quantity,
                Instant.now(),
                confirmedBy));

        logger.info("Reservation confirmed: {} ({} units)", reservationId.value(), quantity);
    }

    /**
     * Confirms a reservation (shorter method name).
     */
    public void confirm(ReservationId reservationId, String confirmedBy) {
        confirmReservation(reservationId, confirmedBy);
    }

    /**
     * Releases a reservation (shorter method name).
     */
    public void release(ReservationId reservationId, String releasedBy) {
        releaseReservation(reservationId, releasedBy);
    }

    /**
     * Replenishes stock (new inventory arrives).
     */
    public void replenish(int quantity, String replenishedBy) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Replenishment quantity must be positive");
        }

        int newTotal = totalQuantity + quantity;

        raiseEvent(new StockReplenishedEvent(
                UUID.randomUUID().toString(),
                stockId.value(),
                quantity,
                newTotal,
                Instant.now(),
                replenishedBy));

        logger.info("Stock replenished: {} units added (new total: {})", quantity, newTotal);
    }

    /**
     * Applies events to rebuild aggregate state.
     */
    @Override
    public void apply(DomainEvent event) {
        switch (event) {
            case StockInitializedEvent e -> applyStockInitialized(e);
            case StockReservedEvent e -> applyStockReserved(e);
            case ReservationReleasedEvent e -> applyReservationReleased(e);
            case ReservationConfirmedEvent e -> applyReservationConfirmed(e);
            case StockReplenishedEvent e -> applyStockReplenished(e);
            default -> logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyStockInitialized(StockInitializedEvent event) {
        this.stockId = StockId.of(event.aggregateId());
        this.productId = ProductId.of(event.productId());
        this.totalQuantity = event.quantity();
        this.availableQuantity = event.quantity();
        setAggregateId(event.aggregateId());
    }

    private void applyStockReserved(StockReservedEvent event) {
        this.availableQuantity -= event.quantity();
        this.reservations.put(ReservationId.of(event.reservationId()), event.quantity());
    }

    private void applyReservationReleased(ReservationReleasedEvent event) {
        ReservationId reservationId = ReservationId.of(event.reservationId());
        this.availableQuantity += event.quantity();
        this.reservations.remove(reservationId);
    }

    private void applyReservationConfirmed(ReservationConfirmedEvent event) {
        ReservationId reservationId = ReservationId.of(event.reservationId());
        this.totalQuantity -= event.quantity();
        this.reservations.remove(reservationId);
    }

    private void applyStockReplenished(StockReplenishedEvent event) {
        this.totalQuantity = event.newTotalQuantity();
        this.availableQuantity += event.quantityAdded();
    }

    // Getters

    public StockId getStockId() {
        return stockId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservations.values().stream().mapToInt(Integer::intValue).sum();
    }

    public StockStatus getStatus() {
        if (availableQuantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        } else if (availableQuantity <= LOW_STOCK_THRESHOLD) {
            return StockStatus.LOW_STOCK;
        } else {
            return StockStatus.AVAILABLE;
        }
    }

    public boolean isAvailable() {
        return availableQuantity > 0;
    }

    public boolean canReserve(int quantity) {
        return availableQuantity >= quantity;
    }
}
