package com.konasl.cart.application.query;

import com.konasl.common.cqrs.Query;

import java.util.UUID;

/**
 * Query to retrieve cart details.
 */
public record GetCartQuery(String cartId) implements Query<CartDto> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
