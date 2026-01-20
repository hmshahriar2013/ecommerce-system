package com.konasl.pricing.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Price aggregate root in the Pricing bounded context.
 * 
 * BUSINESS RULES:
 * - Price must be positive
 * - Each product has exactly one active price
 * - Price changes emit new events (for audit trail)
 */
public class Price extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(Price.class);

    private ProductId productId;
    private Money price;

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public Price() {
        super();
    }

    /**
     * Sets price for a product (command method).
     */
    public Price(ProductId productId, Money price, String setBy) {
        super();

        validatePrice(price);

        String priceId = UUID.randomUUID().toString();

        raiseEvent(new PriceSetEvent(
                UUID.randomUUID().toString(),
                priceId,
                productId.value(),
                price.amount(),
                price.currency(),
                Instant.now(),
                setBy));

        logger.info("Price set for product {}: {} {}", productId.value(), price.amount(), price.currency());
    }

    /**
     * Applies events to rebuild aggregate state.
     */
    @Override
    public void apply(DomainEvent event) {
        if (event instanceof PriceSetEvent e) {
            applyPriceSet(e);
        } else {
            logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyPriceSet(PriceSetEvent event) {
        this.productId = ProductId.of(event.productId());
        this.price = Money.of(event.amount(), event.currency());
        setAggregateId(event.aggregateId());
    }

    private void validatePrice(Money price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
    }

    // Getters

    public ProductId getProductId() {
        return productId;
    }

    public Money getPrice() {
        return price;
    }
}
