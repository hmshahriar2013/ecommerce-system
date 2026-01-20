package com.konasl.inventory.application.handler;

import com.konasl.common.cqrs.QueryHandler;
import com.konasl.inventory.application.port.StockReadRepository;
import com.konasl.inventory.application.query.GetStockQuery;
import com.konasl.inventory.application.query.StockDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles getting stock information by product ID.
 */
@Service
public class GetStockQueryHandler implements QueryHandler<GetStockQuery, StockDto> {

    private static final Logger logger = LoggerFactory.getLogger(GetStockQueryHandler.class);

    private final StockReadRepository repository;

    public GetStockQueryHandler(StockReadRepository repository) {
        this.repository = repository;
    }

    @Override
    public StockDto handle(GetStockQuery query) {
        logger.debug("Getting stock for product: {}", query.productId());

        return repository.findByProductId(query.productId())
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + query.productId()));
    }

    @Override
    public Class<GetStockQuery> getQueryType() {
        return GetStockQuery.class;
    }
}
