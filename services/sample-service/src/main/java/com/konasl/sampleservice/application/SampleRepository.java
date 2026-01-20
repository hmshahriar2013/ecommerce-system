package com.konasl.sampleservice.application;

import com.konasl.sampleservice.domain.Sample;

/**
 * OUTPUT PORT (driven port) for persistence operations.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is a PURE INTERFACE defining the contract for persistence
 * - Belongs to the application layer (inner hexagon)
 * - NO framework-specific annotations or types
 * - Implementation will be in the outbound adapter layer
 * - The application layer depends on this abstraction, not on concrete
 * implementations
 */
public interface SampleRepository {

    /**
     * Save a sample to the repository
     */
    Sample save(Sample sample);

    /**
     * Find a sample by its ID
     * 
     * @return Sample if found, null otherwise
     */
    Sample findById(String id);

    /**
     * Delete a sample by its ID
     */
    void deleteById(String id);
}
