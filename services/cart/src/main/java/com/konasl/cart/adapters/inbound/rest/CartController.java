package com.konasl.cart.adapters.inbound.rest;

import com.konasl.cart.application.command.AddToCartCommand;
import com.konasl.cart.application.command.RemoveFromCartCommand;
import com.konasl.cart.application.query.CartDto;
import com.konasl.cart.application.query.GetCartQuery;
import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.cqrs.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for cart operations.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CommandHandler<AddToCartCommand> addToCartHandler;
    private final CommandHandler<RemoveFromCartCommand> removeFromCartHandler;
    private final QueryHandler<GetCartQuery, CartDto> getCartHandler;

    public CartController(
            CommandHandler<AddToCartCommand> addToCartHandler,
            CommandHandler<RemoveFromCartCommand> removeFromCartHandler,
            QueryHandler<GetCartQuery, CartDto> getCartHandler) {
        this.addToCartHandler = addToCartHandler;
        this.removeFromCartHandler = removeFromCartHandler;
        this.getCartHandler = getCartHandler;
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@RequestBody AddToCartRequest request) {
        logger.info("Adding to cart - User: {}, Product: {}, Quantity: {}",
                request.userId(), request.productId(), request.quantity());

        var command = new AddToCartCommand(
                request.cartId(),
                request.userId(),
                request.productId(),
                request.quantity(),
                request.price(),
                request.currency());

        addToCartHandler.handle(command);

        return ResponseEntity.ok(new CartResponse("Item added to cart successfully"));
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable String cartId,
            @PathVariable String productId) {

        logger.info("Removing product {} from cart {}", productId, cartId);

        var command = new RemoveFromCartCommand(cartId, productId);
        removeFromCartHandler.handle(command);

        return ResponseEntity.ok(new CartResponse("Item removed from cart"));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDetailResponse> getCart(@PathVariable String cartId) {
        logger.debug("Getting cart: {}", cartId);

        var query = new GetCartQuery(cartId);
        CartDto cart = getCartHandler.handle(query);

        List<CartItemDto> items = cart.items().stream()
                .map(item -> new CartItemDto(item.productId(), item.quantity(), item.price()))
                .toList();

        return ResponseEntity.ok(new CartDetailResponse(
                cart.cartId(),
                cart.userId(),
                items,
                cart.totalAmount()));
    }

    public record AddToCartRequest(
            String cartId,
            String userId,
            String productId,
            int quantity,
            BigDecimal price,
            String currency) {
    }

    public record CartResponse(
            String message) {
    }

    public record CartDetailResponse(
            String cartId,
            String userId,
            List<CartItemDto> items,
            BigDecimal totalAmount) {
    }

    public record CartItemDto(
            String productId,
            int quantity,
            BigDecimal price) {
    }
}
