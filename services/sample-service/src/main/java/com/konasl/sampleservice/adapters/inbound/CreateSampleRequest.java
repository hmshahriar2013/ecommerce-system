package com.konasl.sampleservice.adapters.inbound;

/**
 * REST request DTO for creating a sample.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is a DATA TRANSFER OBJECT for the adapter layer
 * - Separate from domain entities
 * - Uses Java records for immutability
 */
public record CreateSampleRequest(
        String id,
        String name,
        String description) {
}
