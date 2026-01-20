package com.konasl.catalog.adapters.inbound.rest;

import com.konasl.catalog.application.handler.GetProductQueryHandler;
import com.konasl.catalog.application.handler.ListPublishedProductsQueryHandler;
import com.konasl.catalog.application.query.GetProductQuery;
import com.konasl.catalog.application.query.ListPublishedProductsQuery;
import com.konasl.catalog.application.query.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product query operations.
 * 
 * HEXAGONAL ARCHITECTURE:
 * - This is an inbound adapter
 * - Delegates to query handlers in application layer
 * - No business logic here
 */
@RestController
@RequestMapping("/api/catalog/products")
public class ProductQueryController {

    private static final Logger logger = LoggerFactory.getLogger(ProductQueryController.class);

    private final GetProductQueryHandler getProductHandler;
    private final ListPublishedProductsQueryHandler listPublishedHandler;

    public ProductQueryController(GetProductQueryHandler getProductHandler,
            ListPublishedProductsQueryHandler listPublishedHandler) {
        this.getProductHandler = getProductHandler;
        this.listPublishedHandler = listPublishedHandler;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String productId) {
        logger.debug("Getting product: {}", productId);

        GetProductQuery query = new GetProductQuery(productId);
        ProductDto product = getProductHandler.handle(query);

        return ResponseEntity.ok(product);
    }

    @GetMapping("/published")
    public ResponseEntity<List<ProductDto>> listPublishedProducts() {
        logger.debug("Listing all published products");

        ListPublishedProductsQuery query = new ListPublishedProductsQuery();
        List<ProductDto> products = listPublishedHandler.handle(query);

        return ResponseEntity.ok(products);
    }
}
