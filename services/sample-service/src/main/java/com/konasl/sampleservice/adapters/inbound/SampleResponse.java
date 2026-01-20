package com.konasl.sampleservice.adapters.inbound;

import com.konasl.sampleservice.domain.Sample;
import com.konasl.sampleservice.domain.SampleStatus;

/**
 * REST response DTO for sample data.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is a DATA TRANSFER OBJECT for the adapter layer
 * - Converts domain entities to API responses
 * - Uses Java records for immutability
 */
public record SampleResponse(
        String id,
        String name,
        String description,
        String status) {
    /**
     * Convert domain entity to response DTO
     */
    public static SampleResponse fromDomain(Sample sample) {
        return new SampleResponse(
                sample.getId(),
                sample.getName(),
                sample.getDescription(),
                sample.getStatus().name());
    }
}
