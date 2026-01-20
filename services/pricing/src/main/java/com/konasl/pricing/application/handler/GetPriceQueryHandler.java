package com.konasl.pricing.application.handler;

import com.konasl.common.cqrs.QueryHandler;
import com.konasl.pricing.application.port.PriceReadRepository;
import com.konasl.pricing.application.query.GetPriceQuery;
import com.konasl.pricing.application.query.PriceDto;
import org.springframework.stereotype.Component;

/**
 * Handler for GetPriceQuery.
 */
@Component
public class GetPriceQueryHandler implements QueryHandler<GetPriceQuery, PriceDto> {

    private final PriceReadRepository readRepository;

    public GetPriceQueryHandler(PriceReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Override
    public PriceDto handle(GetPriceQuery query) {
        return readRepository.findById(query.productId())
                .orElseThrow(() -> new RuntimeException("Price not found for product: " + query.productId()));
    }

    @Override
    public Class<GetPriceQuery> getQueryType() {
        return GetPriceQuery.class;
    }
}
