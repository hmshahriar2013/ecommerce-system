package com.konasl.inventory.application.handler;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.inventory.application.command.ReleaseReservationCommand;
import com.konasl.inventory.application.port.InventoryOutboxRepository;
import com.konasl.inventory.application.port.StockEventStore;
import com.konasl.inventory.domain.ReservationId;
import com.konasl.inventory.domain.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles releasing a stock reservation.
 */
@Service
public class ReleaseReservationCommandHandler implements CommandHandler<ReleaseReservationCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseReservationCommandHandler.class);

    private final StockEventStore eventStore;
    private final InventoryOutboxRepository outboxRepository;

    public ReleaseReservationCommandHandler(
            StockEventStore eventStore,
            InventoryOutboxRepository outboxRepository) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void handle(ReleaseReservationCommand command) {
        logger.info("Releasing reservation: {}", command.reservationId());

        // Load Stock aggregate
        Stock stock = eventStore.load(command.stockId());

        // Release reservation
        stock.release(ReservationId.of(command.reservationId()), "system");

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

        logger.info("Reservation released successfully: {}", command.reservationId());
    }

    @Override
    public Class<ReleaseReservationCommand> getCommandType() {
        return ReleaseReservationCommand.class;
    }
}
