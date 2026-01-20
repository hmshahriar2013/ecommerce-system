package com.konasl.sampleservice.application;

import com.konasl.sampleservice.domain.Sample;

/**
 * Use case for activating a Sample.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - Orchestrates domain operations
 * - Depends only on domain and ports
 * - Framework-agnostic
 */
public class ActivateSampleUseCase {

    private final SampleRepository sampleRepository;

    public ActivateSampleUseCase(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    /**
     * Execute the use case to activate a sample
     */
    public Sample execute(String id) {
        Sample sample = sampleRepository.findById(id);
        if (sample == null) {
            throw new IllegalArgumentException("Sample not found: " + id);
        }

        // Domain behavior
        sample.activate();

        // Persist changes
        return sampleRepository.save(sample);
    }
}
