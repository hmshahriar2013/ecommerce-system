package com.konasl.catalog.application.query;

import com.konasl.common.cqrs.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to list all published products.
 */
public record ListPublishedProductsQuery() implements Query<List<ProductDto>> {

    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
