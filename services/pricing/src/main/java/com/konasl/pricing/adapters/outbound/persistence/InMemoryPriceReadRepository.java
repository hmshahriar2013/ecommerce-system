package com.konasl.pricing.adapters.outbound.persistence;

import com.konasl.pricing.application.port.PriceReadRepository;
import com.konasl.pricing.application.query.PriceDto;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of PriceReadRepository.
 */
@Repository
public class InMemoryPriceReadRepository implements PriceReadRepository {

    private final Map<String, PriceDto> prices = new ConcurrentHashMap<>();

    @Override
    public Optional<PriceDto> findById(String productId) {
        return Optional.ofNullable(prices.get(productId));
    }

    @Override
    public void save(PriceDto price) {
        prices.put(price.productId(), price);
    }
}
