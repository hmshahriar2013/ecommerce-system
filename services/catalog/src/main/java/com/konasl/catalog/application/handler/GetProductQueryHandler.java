package com.konasl.catalog.application.handler;

import com.konasl.catalog.application.port.ProductReadRepository;
import com.konasl.catalog.application.query.GetProductQuery;
import com.konasl.catalog.application.query.ProductDto;
import com.konasl.common.cqrs.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for GetProductQuery.
 */
@Component
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, ProductDto> {

    private static final Logger logger = LoggerFactory.getLogger(GetProductQueryHandler.class);

    private final ProductReadRepository readRepository;

    public GetProductQueryHandler(ProductReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Override
    public ProductDto handle(GetProductQuery query) {
        logger.debug("Retrieving product: {}", query.productId());

        return readRepository.findById(query.productId())
                .orElseThrow(() -> {
                    logger.error("Product not found: {}", query.productId());
                    return new IllegalArgumentException("Product not found: " + query.productId());
                });
    }

    @Override
    public Class<GetProductQuery> getQueryType() {
        return GetProductQuery.class;
    }
}
