package com.konasl.inventory.application.handler;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.inventory.application.command.ReplenishStockCommand;
import com.konasl.inventory.application.port.InventoryOutboxRepository;
import com.konasl.inventory.application.port.StockEventStore;
import com.konasl.inventory.domain.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles stock replenishment.
 */
@Service
public class ReplenishStockCommandHandler implements CommandHandler<ReplenishStockCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ReplenishStockCommandHandler.class);

    private final StockEventStore eventStore;
    private final InventoryOutboxRepository outboxRepository;

    public ReplenishStockCommandHandler(
            StockEventStore eventStore,
            InventoryOutboxRepository outboxRepository) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void handle(ReplenishStockCommand command) {
        logger.info("Replenishing stock: {} with quantity: {}",
                command.stockId(), command.quantity());

        // Load Stock aggregate
        Stock stock = eventStore.load(command.stockId());

        // Replenish stock
        stock.replenish(command.quantity(), "warehouse-system");

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

        logger.info("Stock replenished successfully: {}", command.stockId());
    }

    @Override
    public Class<ReplenishStockCommand> getCommandType() {
        return ReplenishStockCommand.class;
    }
}
