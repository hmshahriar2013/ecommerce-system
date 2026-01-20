package com.konasl.inventory.application.handler;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.inventory.application.command.ReserveStockCommand;
import com.konasl.inventory.application.port.InventoryOutboxRepository;
import com.konasl.inventory.application.port.StockEventStore;
import com.konasl.inventory.domain.ReservationId;
import com.konasl.inventory.domain.Stock;
import com.konasl.inventory.domain.StockId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles stock reservation.
 */
@Service
public class ReserveStockCommandHandler implements CommandHandler<ReserveStockCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ReserveStockCommandHandler.class);

    private final StockEventStore eventStore;
    private final InventoryOutboxRepository outboxRepository;

    public ReserveStockCommandHandler(
            StockEventStore eventStore,
            InventoryOutboxRepository outboxRepository) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void handle(ReserveStockCommand command) {
        logger.info("Reserving stock {} for product: {}, quantity: {}",
                command.reservationId(), command.productId(), command.quantity());

        // Load Stock aggregate from event store
        Stock stock = eventStore.load(command.stockId());

        // Reserve stock
        stock.reserve(
                ReservationId.of(command.reservationId()),
                command.stockId(),
                command.quantity(),
                "order-service");

        // Save new events
        eventStore.save(stock);

        // Save to outbox
        for (DomainEvent event : stock.getUncommittedEvents()) {
            outboxRepository.save(OutboxEvent.fromDomainEvent(
                    event.getEventId(),
                    event,
                    event.toString()));
        }

        // Mark events as committed
        stock.markEventsAsCommitted();

        logger.info("Stock reserved successfully: {}", command.reservationId());
    }

    @Override
    public Class<ReserveStockCommand> getCommandType() {
        return ReserveStockCommand.class;
    }
}
