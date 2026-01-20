package com.konasl.sampleservice.application;

import com.konasl.sampleservice.domain.Sample;

/**
 * Use case for creating a new Sample.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - Contains application-specific business logic and orchestration
 * - Depends ONLY on domain entities and output ports (interfaces)
 * - NO dependencies on adapters or infrastructure
 * - NO Spring annotations here (they belong in infrastructure layer)
 */
public class CreateSampleUseCase {

    private final SampleRepository sampleRepository;

    public CreateSampleUseCase(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    /**
     * Execute the use case to create a sample
     */
    public Sample execute(String id, String name, String description) {
        // Domain logic: Create the sample using factory method
        Sample sample = Sample.create(id, name, description);

        // Persist through the port
        return sampleRepository.save(sample);
    }
}
