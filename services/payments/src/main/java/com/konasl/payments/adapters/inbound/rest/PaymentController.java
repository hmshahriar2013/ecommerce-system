package com.konasl.payments.adapters.inbound.rest;

import com.konasl.common.cqrs.CommandHandler;
import com.konasl.common.cqrs.QueryHandler;
import com.konasl.payments.application.command.ProcessPaymentCommand;
import com.konasl.payments.application.query.GetPaymentQuery;
import com.konasl.payments.application.query.PaymentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final CommandHandler<ProcessPaymentCommand> processPaymentHandler;
    private final QueryHandler<GetPaymentQuery, PaymentDto> getPaymentHandler;

    public PaymentController(
            CommandHandler<ProcessPaymentCommand> processPaymentHandler,
            QueryHandler<GetPaymentQuery, PaymentDto> getPaymentHandler) {
        this.processPaymentHandler = processPaymentHandler;
        this.getPaymentHandler = getPaymentHandler;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody ProcessPaymentRequest request) {
        logger.info("Processing payment for order {}: {} {}",
                request.orderId(), request.amount(), request.currency());

        var command = new ProcessPaymentCommand(
                request.orderId(),
                request.amount(),
                request.currency());

        processPaymentHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PaymentResponse(command.getCommandId(), "SUCCESSFUL", "Payment processed successfully"));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponse> getPayment(@PathVariable String paymentId) {
        logger.debug("Getting payment: {}", paymentId);

        var query = new GetPaymentQuery(paymentId);
        PaymentDto payment = getPaymentHandler.handle(query);

        return ResponseEntity.ok(new PaymentDetailResponse(
                payment.paymentId(),
                payment.orderId(),
                payment.amount(),
                payment.currency(),
                payment.status()));
    }

    public record ProcessPaymentRequest(
            String orderId,
            BigDecimal amount,
            String currency) {
    }

    public record PaymentResponse(
            String paymentId,
            String status,
            String message) {
    }

    public record PaymentDetailResponse(
            String paymentId,
            String orderId,
            BigDecimal amount,
            String currency,
            String status) {
    }
}
