package com.konasl.orders.application.handler;

import com.konasl.common.cqrs.QueryHandler;
import com.konasl.orders.application.port.OrderReadRepository;
import com.konasl.orders.application.query.GetOrderQuery;
import com.konasl.orders.application.query.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles getting order by ID.
 */
@Service
public class GetOrderQueryHandler implements QueryHandler<GetOrderQuery, OrderDto> {

    private static final Logger logger = LoggerFactory.getLogger(GetOrderQueryHandler.class);

    private final OrderReadRepository repository;

    public GetOrderQueryHandler(OrderReadRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderDto handle(GetOrderQuery query) {
        logger.debug("Getting order: {}", query.orderId());

        return repository.findById(query.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + query.orderId()));
    }

    @Override
    public Class<GetOrderQuery> getQueryType() {
        return GetOrderQuery.class;
    }
}
