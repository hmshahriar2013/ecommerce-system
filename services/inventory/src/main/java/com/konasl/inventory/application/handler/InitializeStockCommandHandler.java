package com.konasl.inventory.application.handler;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.domain.DomainEvent;
import com.konasl.common.outbox.OutboxEvent;
import com.konasl.inventory.application.command.InitializeStockCommand;
import com.konasl.inventory.application.port.InventoryOutboxRepository;
import com.konasl.inventory.application.port.StockEventStore;
import com.konasl.inventory.domain.ProductId;
import com.konasl.inventory.domain.Stock;
import com.konasl.inventory.domain.StockId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles initialization of stock for a product.
 */
@Service
public class InitializeStockCommandHandler implements CommandHandler<InitializeStockCommand> {

    private static final Logger logger = LoggerFactory.getLogger(InitializeStockCommandHandler.class);

    private final StockEventStore eventStore;
    private final InventoryOutboxRepository outboxRepository;

    public InitializeStockCommandHandler(
            StockEventStore eventStore,
            InventoryOutboxRepository outboxRepository) {
        this.eventStore = eventStore;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void handle(InitializeStockCommand command) {
        logger.info("Initializing stock for product: {} with quantity: {}",
                command.productId(), command.initialQuantity());

        // Create new Stock aggregate
        Stock stock = new Stock(
                StockId.generate(),
                ProductId.of(command.productId()),
                command.initialQuantity(),
                "system");

        // Save to event store
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

        logger.info("Stock initialized successfully for product: {}", command.productId());
    }

    @Override
    public Class<InitializeStockCommand> getCommandType() {
        return InitializeStockCommand.class;
    }
}
