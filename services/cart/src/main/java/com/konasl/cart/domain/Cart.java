package com.konasl.cart.domain;

import com.konasl.common.domain.AggregateRoot;
import com.konasl.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart aggregate root in the Cart bounded context.
 * 
 * BUSINESS RULES:
 * - Each user has one active cart
 * - Cart items have product ID and quantity
 * - Quantity must be positive
 * - Empty carts are allowed
 */
public class Cart extends AggregateRoot {

    private static final Logger logger = LoggerFactory.getLogger(Cart.class);

    private CartId cartId;
    private UserId userId;
    private List<CartItem> items = new ArrayList<>();

    /**
     * Default constructor for event sourcing reconstruction.
     */
    public Cart() {
        super();
    }

    /**
     * Creates a new cart for a user.
     */
    public Cart(CartId cartId, UserId userId, String createdBy) {
        super();
        this.cartId = cartId;
        this.userId = userId;
        setAggregateId(cartId.value());

        raiseEvent(new CartCreatedEvent(
                UUID.randomUUID().toString(),
                cartId.value(),
                userId.value(),
                Instant.now(),
                createdBy));

        logger.info("Cart created for user: {}", userId.value());
    }

    /**
     * Adds item to cart or updates quantity if already exists.
     */
    public void addItem(ProductId productId, int quantity, Money price, String addedBy) {
        validateQuantity(quantity);

        raiseEvent(new ItemAddedToCartEvent(
                UUID.randomUUID().toString(),
                cartId.value(),
                userId.value(),
                productId.value(),
                quantity,
                price.amount(),
                price.currency(),
                Instant.now(),
                addedBy));

        logger.info("Item added to cart {}: {} x{}", cartId.value(), productId.value(), quantity);
    }

    /**
     * Removes item from cart.
     */
    public void removeItem(ProductId productId, String removedBy) {
        raiseEvent(new ItemRemovedFromCartEvent(
                UUID.randomUUID().toString(),
                cartId.value(),
                productId.value(),
                Instant.now(),
                removedBy));

        logger.info("Item removed from cart {}: {}", cartId.value(), productId.value());
    }

    /**
     * Applies events to rebuild aggregate state.
     */
    @Override
    public void apply(DomainEvent event) {
        if (event instanceof CartCreatedEvent e) {
            applyCartCreated(e);
        } else if (event instanceof ItemAddedToCartEvent e) {
            applyItemAdded(e);
        } else if (event instanceof ItemRemovedFromCartEvent e) {
            applyItemRemoved(e);
        } else {
            logger.warn("Unknown event type: {}", event.getClass().getName());
        }
    }

    private void applyCartCreated(CartCreatedEvent event) {
        this.cartId = CartId.of(event.aggregateId());
        this.userId = UserId.of(event.userId());
        setAggregateId(event.aggregateId());
    }

    private void applyItemAdded(ItemAddedToCartEvent event) {
        ProductId productId = ProductId.of(event.productId());

        // Find existing item
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.productId().equals(productId)) {
                items.set(i, item.withQuantity(item.quantity() + event.quantity()));
                found = true;
                break;
            }
        }

        if (!found) {
            items.add(new CartItem(productId, event.quantity()));
        }
    }

    private void applyItemRemoved(ItemRemovedFromCartEvent event) {
        ProductId productId = ProductId.of(event.productId());
        items.removeIf(item -> item.productId().equals(productId));
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    // Getters

    public CartId getCartId() {
        return cartId;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::quantity).sum();
    }
}
