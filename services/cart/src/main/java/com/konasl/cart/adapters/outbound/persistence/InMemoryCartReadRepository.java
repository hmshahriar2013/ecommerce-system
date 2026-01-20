package com.konasl.cart.adapters.outbound.persistence;

import com.konasl.cart.application.port.CartReadRepository;
import com.konasl.cart.application.query.CartDto;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of CartReadRepository.
 */
@Repository
public class InMemoryCartReadRepository implements CartReadRepository {

    private final Map<String, CartDto> carts = new ConcurrentHashMap<>();
    private final Map<String, String> userIdToCartId = new ConcurrentHashMap<>();

    @Override
    public Optional<CartDto> findById(String cartId) {
        return Optional.ofNullable(carts.get(cartId));
    }

    @Override
    public Optional<CartDto> findByUserId(String userId) {
        String cartId = userIdToCartId.get(userId);
        if (cartId == null) {
            return Optional.empty();
        }
        return findById(cartId);
    }

    @Override
    public void save(CartDto cart) {
        carts.put(cart.cartId(), cart);
        userIdToCartId.put(cart.userId(), cart.cartId());
    }
}
