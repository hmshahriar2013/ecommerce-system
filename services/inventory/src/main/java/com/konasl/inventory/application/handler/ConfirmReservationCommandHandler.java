package com.konasl.inventory.application.handler;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.inventory.application.command.ConfirmReservationCommand;
import com.konasl.inventory.application.port.InventoryOutboxRepository;
import com.konasl.inventory.application.port.StockEventStore;
import com.konasl.inventory.domain.ReservationId;
import com.konasl.inventory.domain.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles confirming a stock reservation.
 */
@Service
public class ConfirmReservationCommandHandler implements CommandHandler<ConfirmReservationCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmReservationCommandHandler.class);

    private final StockEventStore eventStore;
    private final InventoryOutboxRepository outboxRepository;

    public ConfirmReservationCommandHandler(
            StockEventStore eventStore,
            InventoryOutboxRepository outboxRepository) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void handle(ConfirmReservationCommand command) {
        logger.info("Confirming reservation: {}", command.reservationId());

        // Load Stock aggregate
        Stock stock = eventStore.load(command.stockId());

        // Confirm reservation
        stock.confirm(ReservationId.of(command.reservationId()), "order-service");

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

        logger.info("Reservation confirmed successfully: {}", command.reservationId());
    }

    @Override
    public Class<ConfirmReservationCommand> getCommandType() {
        return ConfirmReservationCommand.class;
    }
}
