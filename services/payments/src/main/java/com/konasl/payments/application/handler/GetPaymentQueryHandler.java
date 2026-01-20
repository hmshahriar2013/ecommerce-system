package com.konasl.payments.application.handler;

import com.konasl.common.cqrs.QueryHandler;
import com.konasl.payments.application.port.PaymentReadRepository;
import com.konasl.payments.application.query.GetPaymentQuery;
import com.konasl.payments.application.query.PaymentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles getting payment by ID.
 */
@Service
public class GetPaymentQueryHandler implements QueryHandler<GetPaymentQuery, PaymentDto> {

    private static final Logger logger = LoggerFactory.getLogger(GetPaymentQueryHandler.class);

    private final PaymentReadRepository repository;

    public GetPaymentQueryHandler(PaymentReadRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentDto handle(GetPaymentQuery query) {
        logger.debug("Getting payment: {}", query.paymentId());

        return repository.findById(query.paymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + query.paymentId()));
    }

    @Override
    public Class<GetPaymentQuery> getQueryType() {
        return GetPaymentQuery.class;
    }
}
