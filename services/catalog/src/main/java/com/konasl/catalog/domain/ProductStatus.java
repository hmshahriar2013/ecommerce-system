package com.konasl.catalog.domain;

/**
 * Status of a product in the catalog.
 * 
 * BUSINESS RULES:
 * - DRAFT: Product is being prepared, not visible to customers
 * - PUBLISHED: Product is live and available for purchase
 * - UNPUBLISHED: Product was published but is now hidden (soft delete)
 */
public enum ProductStatus {
    /**
     * Product is in draft state, not visible to customers.
     */
    DRAFT,

    /**
     * Product is published and visible to customers.
     */
    PUBLISHED,

    /**
     * Product was published but is now hidden from customers.
     */
    UNPUBLISHED
}
