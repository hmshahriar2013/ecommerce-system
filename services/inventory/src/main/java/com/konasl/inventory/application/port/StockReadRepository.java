package com.konasl.inventory.application.port;

import com.konasl.inventory.application.query.StockDto;

import java.util.Optional;

/**
 * Read repository port for Stock queries.
 */
public interface StockReadRepository {

    /**
     * Find stock by product ID.
     */
    Optional<StockDto> findByProductId(String productId);

    /**
     * Save or update stock DTO.
     */
    void save(StockDto stockDto);
}
