package com.konasl.pricing.application.port;

import com.konasl.pricing.application.query.PriceDto;

import java.util.Optional;

/**
 * Port for read-side price repository.
 */
public interface PriceReadRepository {

    Optional<PriceDto> findById(String productId);

    void save(PriceDto price);
}
