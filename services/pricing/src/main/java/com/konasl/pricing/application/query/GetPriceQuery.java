package com.konasl.pricing.application.query;

import com.konasl.common.cqrs.Query;

/**
 * Query to retrieve price details.
 */
public record GetPriceQuery(String productId) implements Query<PriceDto> {
    @Override
    public String getQueryId() {
        return productId;
    }
}
