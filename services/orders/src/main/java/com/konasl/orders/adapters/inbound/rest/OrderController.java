package com.konasl.orders.adapters.inbound.rest;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.cqrs.QueryHandler;
import com.konasl.orders.application.command.PlaceOrderCommand;
import com.konasl.orders.application.query.GetOrderQuery;
import com.konasl.orders.application.query.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for order operations.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final CommandHandler<PlaceOrderCommand> placeOrderHandler;
    private final QueryHandler<GetOrderQuery, OrderDto> getOrderHandler;

    public OrderController(
            CommandHandler<PlaceOrderCommand> placeOrderHandler,
            QueryHandler<GetOrderQuery, OrderDto> getOrderHandler) {
        this.placeOrderHandler = placeOrderHandler;
        this.getOrderHandler = getOrderHandler;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        logger.info("Placing order for user: {} with {} items", request.userId(), request.items().size());

        // Map controller DTOs to command DTOs
        List<PlaceOrderCommand.OrderItemDto> commandItems = request.items().stream()
                .map(item -> new PlaceOrderCommand.OrderItemDto(item.productId(), item.quantity()))
                .toList();

        var command = new PlaceOrderCommand(request.userId(), commandItems);
        placeOrderHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new OrderResponse(command.getCommandId(), "PENDING_PAYMENT", "Order placed successfully"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable String orderId) {
        logger.debug("Getting order: {}", orderId);

        var query = new GetOrderQuery(orderId);
        OrderDto order = getOrderHandler.handle(query);

        var items = order.items().stream()
                .map(item -> new OrderItemDto(item.productId(), item.quantity()))
                .toList();

        return ResponseEntity.ok(new OrderDetailResponse(
                order.orderId(),
                order.userId(),
                items,
                order.totalAmount(),
                order.status()));
    }

    public record PlaceOrderRequest(
            String userId,
            List<OrderItemDto> items) {
    }

    public record OrderItemDto(
            String productId,
            int quantity) {
    }

    public record OrderResponse(
            String orderId,
            String status,
            String message) {
    }

    public record OrderDetailResponse(
            String orderId,
            String userId,
            List<OrderItemDto> items,
            String totalAmount,
            String status) {
    }
}
