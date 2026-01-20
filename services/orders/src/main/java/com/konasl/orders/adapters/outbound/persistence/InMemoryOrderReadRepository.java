package com.konasl.orders.adapters.outbound.persistence;

import com.konasl.orders.application.port.OrderReadRepository;
import com.konasl.orders.application.query.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of order read repository.
 */
@Repository
public class InMemoryOrderReadRepository implements OrderReadRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrderReadRepository.class);

    private final ConcurrentHashMap<String, OrderDto> orders = new ConcurrentHashMap<>();

    @Override
    public Optional<OrderDto> findById(String orderId) {
        logger.debug("Finding order by ID: {}", orderId);
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<OrderDto> findByUserId(String userId) {
        logger.debug("Finding orders by user ID: {}", userId);
        return orders.values().stream()
                .filter(order -> order.userId().equals(userId))
                .toList();
    }

    @Override
    public void save(OrderDto orderDto) {
        orders.put(orderDto.orderId(), orderDto);
        logger.debug("Saved order DTO: {}", orderDto.orderId());
    }
}
