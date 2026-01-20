package com.konasl.payments.application.port;

import com.konasl.common.eventsourcing.EventStore;
import com.konasl.payments.domain.Payment;

/**
 * Event store port for Payment aggregate.
 */
public interface PaymentEventStore extends EventStore<Payment> {
}
