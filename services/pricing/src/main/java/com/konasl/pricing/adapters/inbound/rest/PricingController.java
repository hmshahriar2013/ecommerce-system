package com.konasl.pricing.adapters.inbound.rest;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.cqrs.QueryHandler;
import com.konasl.pricing.application.command.SetPriceCommand;
import com.konasl.pricing.application.query.GetPriceQuery;
import com.konasl.pricing.application.query.PriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for pricing operations.
 */
@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private static final Logger logger = LoggerFactory.getLogger(PricingController.class);

    private final CommandHandler<SetPriceCommand> setPriceHandler;
    private final QueryHandler<GetPriceQuery, PriceDto> getPriceHandler;

    public PricingController(
            CommandHandler<SetPriceCommand> setPriceHandler,
            QueryHandler<GetPriceQuery, PriceDto> getPriceHandler) {
        this.setPriceHandler = setPriceHandler;
        this.getPriceHandler = getPriceHandler;
    }

    @PostMapping("/products/{productId}/price")
    public ResponseEntity<PriceResponse> setPrice(
            @PathVariable String productId,
            @RequestBody SetPriceRequest request) {
        logger.info("Setting price for product {}: {} {}", productId, request.amount(), request.currency());

        var command = new SetPriceCommand(
                productId,
                request.amount(),
                request.currency());

        setPriceHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PriceResponse(productId, "Price set successfully"));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<PriceDetailResponse> getPrice(@PathVariable String productId) {
        logger.debug("Getting price for product: {}", productId);

        var query = new GetPriceQuery(productId);
        PriceDto price = getPriceHandler.handle(query);

        return ResponseEntity.ok(new PriceDetailResponse(
                price.productId(),
                price.amount(),
                price.currency()));
    }

    public record SetPriceRequest(
            BigDecimal amount,
            String currency) {
    }

    public record PriceResponse(
            String productId,
            String message) {
    }

    public record PriceDetailResponse(
            String productId,
            BigDecimal amount,
            String currency) {
    }
}
