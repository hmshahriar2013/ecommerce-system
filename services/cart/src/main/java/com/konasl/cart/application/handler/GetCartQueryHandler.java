package com.konasl.cart.application.handler;

import com.konasl.cart.application.port.CartReadRepository;
import com.konasl.cart.application.query.CartDto;
import com.konasl.cart.application.query.GetCartQuery;
import com.konasl.common.cqrs.QueryHandler;
import org.springframework.stereotype.Component;

/**
 * Handler for GetCartQuery.
 */
@Component
public class GetCartQueryHandler implements QueryHandler<GetCartQuery, CartDto> {

    private final CartReadRepository readRepository;

    public GetCartQueryHandler(CartReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Override
    public CartDto handle(GetCartQuery query) {
        return readRepository.findById(query.cartId())
                .orElseThrow(() -> new RuntimeException("Cart not found: " + query.cartId()));
    }

    @Override
    public Class<GetCartQuery> getQueryType() {
        return GetCartQuery.class;
    }
}
