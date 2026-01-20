package com.konasl.pricing.adapters.inbound.event;

import com.konasl.pricing.application.port.PriceReadRepository;
import com.konasl.pricing.application.query.PriceDto;
import com.konasl.pricing.domain.PriceSetEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Projection that updates the price read model based on domain events.
 */
@Component
public class PriceReadModelProjection {

    private static final Logger logger = LoggerFactory.getLogger(PriceReadModelProjection.class);

    private final PriceReadRepository readRepository;

    public PriceReadModelProjection(PriceReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @EventListener
    public void on(PriceSetEvent event) {
        logger.info("Projecting PriceSetEvent: product {}, amount {}",
                event.aggregateId(), event.amount());

        PriceDto price = new PriceDto(
                event.aggregateId(),
                event.amount(),
                event.currency(),
                "ACTIVE");

        readRepository.save(price);
    }
}
