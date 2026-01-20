package com.konasl.cart.application.port;

import com.konasl.cart.application.query.CartDto;

import java.util.Optional;

/**
 * Port for read-side cart repository.
 */
public interface CartReadRepository {

    Optional<CartDto> findById(String cartId);

    Optional<CartDto> findByUserId(String userId);

    void save(CartDto cart);
}
