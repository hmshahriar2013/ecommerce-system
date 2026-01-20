package com.konasl.orders.application.port;

import com.konasl.orders.application.query.OrderDto;

import java.util.List;
import java.util.Optional;

/**
 * Read repository port for Order queries.
 */
public interface OrderReadRepository {

    /**
     * Find order by ID.
     */
    Optional<OrderDto> findById(String orderId);

    /**
     * Find all orders by user ID.
     */
    List<OrderDto> findByUserId(String userId);

    /**
     * Save or update order DTO.
     */
    void save(OrderDto orderDto);
}
