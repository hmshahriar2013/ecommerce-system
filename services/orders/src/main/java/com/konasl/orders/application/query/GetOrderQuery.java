package com.konasl.orders.application.query;

import com.konasl.common.cqrs.Query;

import java.util.UUID;

/**
 * Query to get order by ID.
 */
public record GetOrderQuery(String orderId) implements Query<OrderDto> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
