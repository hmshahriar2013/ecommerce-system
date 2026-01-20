package com.konasl.catalog.application.port;

import com.konasl.catalog.application.query.ProductDto;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for product read model repository.
 * Implementation will be in the adapter layer.
 */
public interface ProductReadRepository {

    /**
     * Finds a product by ID.
     */
    Optional<ProductDto> findById(String productId);

    /**
     * Finds all published products.
     */
    List<ProductDto> findAllPublished();

    /**
     * Saves or updates a product in the read model.
     */
    void save(ProductDto productDto);

    /**
     * Deletes a product from the read model.
     */
    void delete(String productId);
}
