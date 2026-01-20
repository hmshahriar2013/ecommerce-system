package com.konasl.inventory.adapters.outbound.persistence;

import com.konasl.inventory.application.port.StockReadRepository;
import com.konasl.inventory.application.query.StockDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of stock read repository.
 */
@Repository
public class InMemoryStockReadRepositoryImpl implements StockReadRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStockReadRepositoryImpl.class);

    // Map: productId -> StockDto
    private final ConcurrentHashMap<String, StockDto> stocks = new ConcurrentHashMap<>();

    @Override
    public Optional<StockDto> findByProductId(String productId) {
        logger.debug("Finding stock by product ID: {}", productId);
        return Optional.ofNullable(stocks.get(productId));
    }

    @Override
    public void save(StockDto stockDto) {
        stocks.put(stockDto.productId(), stockDto);
        logger.debug("Saved stock DTO for product: {}", stockDto.productId());
    }
}
