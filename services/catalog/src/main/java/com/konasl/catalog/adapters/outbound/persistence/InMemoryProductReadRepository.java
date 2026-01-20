package com.konasl.catalog.adapters.outbound.persistence;

import com.konasl.catalog.application.port.ProductReadRepository;
import com.konasl.catalog.application.query.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ProductReadRepository for MVP.
 * 
 * TODO: Replace with JPA-based implementation for production.
 * 
 * EVENTUAL CONSISTENCY:
 * - This read model is updated by event handlers
 * - Not updated directly by commands
 */
@Repository
public class InMemoryProductReadRepository implements ProductReadRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryProductReadRepository.class);

    private final Map<String, ProductDto> products = new ConcurrentHashMap<>();

    @Override
    public Optional<ProductDto> findById(String productId) {
        return Optional.ofNullable(products.get(productId));
    }

    @Override
    public List<ProductDto> findAllPublished() {
        return products.values().stream()
                .filter(product -> "PUBLISHED".equals(product.status()))
                .collect(Collectors.toList());
    }

    @Override
    public void save(ProductDto productDto) {
        products.put(productDto.productId(), productDto);
        logger.debug("Saved product to read model: {}", productDto.productId());
    }

    @Override
    public void delete(String productId) {
        products.remove(productId);
        logger.debug("Deleted product from read model: {}", productId);
    }
}
