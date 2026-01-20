package com.konasl.useraccess.domain;

/**
 * User role in the system.
 */
public enum UserRole {
    /**
     * Customer role - can browse and purchase.
     */
    CUSTOMER,

    /**
     * Admin role - can manage catalog, orders, etc.
     */
    ADMIN
}
