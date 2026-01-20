package com.konasl.catalog.application.query;

import com.konasl.common.cqrs.Query;

import java.util.UUID;

/**
 * Query to get product details by ID.
 */
public record GetProductQuery(
        String productId) implements Query<ProductDto> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
