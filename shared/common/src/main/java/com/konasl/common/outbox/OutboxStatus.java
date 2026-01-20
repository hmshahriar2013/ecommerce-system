package com.konasl.common.outbox;

/**
 * Status of an outbox event.
 */
public enum OutboxStatus {
    /**
     * Event is waiting to be dispatched.
     */
    PENDING,

    /**
     * Event has been successfully dispatched to the message broker.
     */
    DISPATCHED,

    /**
     * Event dispatch failed and needs retry.
     */
    FAILED
}
