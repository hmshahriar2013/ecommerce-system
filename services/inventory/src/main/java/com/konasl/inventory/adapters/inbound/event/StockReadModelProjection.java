package com.konasl.inventory.adapters.inbound.event;

import com.konasl.inventory.application.port.StockReadRepository;
import com.konasl.inventory.application.query.StockDto;
import com.konasl.inventory.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Projects domain events to stock read model.
 */
@Component
public class StockReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(StockReadModelProjection.class);

    private final StockReadRepository repository;

    public StockReadModelProjection(StockReadRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onStockInitialized(StockInitializedEvent event) {
        logger.info("Projecting StockInitializedEvent for product: {}", event.productId());

        StockDto dto = new StockDto(
                event.productId(),
                event.quantity(),
                0,
                event.quantity(),
                "AVAILABLE");

        repository.save(dto);
    }

    private String getProductIdFromStockId(String stockId) {
        // For now, return the stockId as productId
        // In a real implementation, you'd look this up from the read model
        return stockId;
    }

    @EventListener
    public void onStockReserved(StockReservedEvent event) {
        String productId = getProductIdFromStockId(event.aggregateId());
        logger.info("Projecting StockReservedEvent for product: {}", productId);

        repository.findByProductId(productId).ifPresent(existing -> {
            StockDto updated = new StockDto(
                    existing.productId(),
                    existing.availableQuantity() - event.quantity(),
                    existing.reservedQuantity() + event.quantity(),
                    existing.totalQuantity(),
                    determineStatus(existing.availableQuantity() - event.quantity()));
            repository.save(updated);
        });
    }

    @EventListener
    public void onReservationReleased(ReservationReleasedEvent event) {
        String productId = getProductIdFromStockId(event.aggregateId());
        logger.info("Projecting ReservationReleasedEvent for product: {}", productId);

        repository.findByProductId(productId).ifPresent(existing -> {
            StockDto updated = new StockDto(
                    existing.productId(),
                    existing.availableQuantity() + event.quantity(),
                    existing.reservedQuantity() - event.quantity(),
                    existing.totalQuantity(),
                    determineStatus(existing.availableQuantity() + event.quantity()));
            repository.save(updated);
        });
    }

    @EventListener
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        String productId = getProductIdFromStockId(event.aggregateId());
        logger.info("Projecting ReservationConfirmedEvent for product: {}", productId);

        repository.findByProductId(productId).ifPresent(existing -> {
            StockDto updated = new StockDto(
                    existing.productId(),
                    existing.availableQuantity(),
                    existing.reservedQuantity() - event.quantity(),
                    existing.totalQuantity() - event.quantity(),
                    determineStatus(existing.availableQuantity()));
            repository.save(updated);
        });
    }

    @EventListener
    public void onStockReplenished(StockReplenishedEvent event) {
        String productId = getProductIdFromStockId(event.aggregateId());
        logger.info("Projecting StockReplenishedEvent for product: {}", productId);

        repository.findByProductId(productId).ifPresent(existing -> {
            StockDto updated = new StockDto(
                    existing.productId(),
                    existing.availableQuantity() + event.quantityAdded(),
                    existing.reservedQuantity(),
                    existing.totalQuantity() + event.quantityAdded(),
                    determineStatus(existing.availableQuantity() + event.quantityAdded()));
            repository.save(updated);
        });
    }

    private String determineStatus(int availableQuantity) {
        if (availableQuantity == 0) {
            return "OUT_OF_STOCK";
        } else if (availableQuantity <= 10) {
            return "LOW_STOCK";
        } else {
            return "AVAILABLE";
        }
    }
}
