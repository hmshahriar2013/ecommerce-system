package com.konasl.sampleservice.infrastructure;

import com.konasl.sampleservice.application.ActivateSampleUseCase;
import com.konasl.sampleservice.application.CreateSampleUseCase;
import com.konasl.sampleservice.application.SampleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for Use Cases - INFRASTRUCTURE LAYER.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is INFRASTRUCTURE code that wires everything together
 * - Creates use case instances and injects dependencies
 * - Connects ports (interfaces) to adapters (implementations)
 * - This is the ONLY place where Spring wiring should happen for use cases
 * 
 * DEPENDENCY FLOW:
 * Controller (inbound adapter) -> Use Case (application) -> Repository Port
 * (application) -> Repository Adapter (outbound)
 */
@Configuration
public class UseCaseConfiguration {

    /**
     * Wire CreateSampleUseCase with its dependencies
     */
    @Bean
    public CreateSampleUseCase createSampleUseCase(SampleRepository sampleRepository) {
        return new CreateSampleUseCase(sampleRepository);
    }

    /**
     * Wire ActivateSampleUseCase with its dependencies
     */
    @Bean
    public ActivateSampleUseCase activateSampleUseCase(SampleRepository sampleRepository) {
        return new ActivateSampleUseCase(sampleRepository);
    }
}
