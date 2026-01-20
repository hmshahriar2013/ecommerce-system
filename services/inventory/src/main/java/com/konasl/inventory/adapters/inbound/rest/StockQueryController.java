package com.konasl.inventory.adapters.inbound.rest;

import com.konasl.common.cqrs.QueryHandler;
import com.konasl.inventory.application.query.GetStockQuery;
import com.konasl.inventory.application.query.StockDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for stock query operations.
 */
@RestController
@RequestMapping("/api/inventory")
public class StockQueryController {

    private static final Logger logger = LoggerFactory.getLogger(StockQueryController.class);

    private final QueryHandler<GetStockQuery, StockDto> getStockHandler;

    public StockQueryController(QueryHandler<GetStockQuery, StockDto> getStockHandler) {
        this.getStockHandler = getStockHandler;
    }

    @GetMapping("/products/{productId}/stock")
    public ResponseEntity<StockDto> getStock(@PathVariable String productId) {
        logger.debug("Getting stock for product: {}", productId);

        var query = new GetStockQuery(productId);
        StockDto stock = getStockHandler.handle(query);

        return ResponseEntity.ok(stock);
    }
}
