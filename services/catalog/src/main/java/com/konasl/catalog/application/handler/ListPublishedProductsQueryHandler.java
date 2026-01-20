package com.konasl.catalog.application.handler;

import com.konasl.catalog.application.port.ProductReadRepository;
import com.konasl.catalog.application.query.ListPublishedProductsQuery;
import com.konasl.catalog.application.query.ProductDto;
import com.konasl.common.cqrs.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for ListPublishedProductsQuery.
 */
@Component
public class ListPublishedProductsQueryHandler
        implements QueryHandler<ListPublishedProductsQuery, List<ProductDto>> {

    private static final Logger logger = LoggerFactory.getLogger(ListPublishedProductsQueryHandler.class);

    private final ProductReadRepository readRepository;

    public ListPublishedProductsQueryHandler(ProductReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Override
    public List<ProductDto> handle(ListPublishedProductsQuery query) {
        logger.debug("Retrieving all published products");
        return readRepository.findAllPublished();
    }

    @Override
    public Class<ListPublishedProductsQuery> getQueryType() {
        return ListPublishedProductsQuery.class;
    }
}
