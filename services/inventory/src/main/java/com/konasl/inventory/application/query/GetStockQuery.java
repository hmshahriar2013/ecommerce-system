package com.konasl.inventory.application.query;

import com.konasl.common.cqrs.Query;

import java.util.UUID;

/**
 * Query to get stock by product ID.
 */
public record GetStockQuery(String productId) implements Query<StockDto> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
