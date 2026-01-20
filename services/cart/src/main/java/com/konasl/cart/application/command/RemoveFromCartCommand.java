package com.konasl.cart.application.command;

import com.konasl.common.cqrs.Command;

import java.util.UUID;

/**
 * Command to remove an item from a cart.
 */
public final class RemoveFromCartCommand implements Command {

    private final String cartId;
    private final String productId;

    public RemoveFromCartCommand(String cartId, String productId) {
        this.cartId = cartId;
        this.productId = productId;
    }

    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }

    public String getCartId() {
        return cartId;
    }

    public String getProductId() {
        return productId;
    }
}
