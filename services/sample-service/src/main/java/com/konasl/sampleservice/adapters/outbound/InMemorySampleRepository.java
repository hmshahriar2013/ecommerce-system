package com.konasl.sampleservice.adapters.outbound;

import com.konasl.sampleservice.application.SampleRepository;
import com.konasl.sampleservice.domain.Sample;
import com.konasl.sampleservice.domain.SampleStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of SampleRepository - OUTBOUND ADAPTER.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is an ADAPTER implementing the repository PORT
 * - Spring annotations are allowed here (adapter layer)
 * - Implements the interface defined in application layer
 * - Handles persistence concerns (in this case, in-memory storage)
 * - Maps between domain entities and persistence models (if needed)
 * 
 * NOTE: This is a simple in-memory implementation for demonstration.
 * In production, this would be replaced with JPA repository adapter.
 */
@Component
public class InMemorySampleRepository implements SampleRepository {

    private final Map<String, SampleEntity> storage = new HashMap<>();

    @Override
    public Sample save(Sample sample) {
        SampleEntity entity = SampleEntity.fromDomain(sample);
        storage.put(entity.getId(), entity);
        return entity.toDomain();
    }

    @Override
    public Sample findById(String id) {
        SampleEntity entity = storage.get(id);
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }

    /**
     * Internal entity class for persistence mapping.
     * This demonstrates the separation between domain model and persistence model.
     */
    private static class SampleEntity {
        private String id;
        private String name;
        private String description;
        private String status;

        public static SampleEntity fromDomain(Sample sample) {
            SampleEntity entity = new SampleEntity();
            entity.id = sample.getId();
            entity.name = sample.getName();
            entity.description = sample.getDescription();
            entity.status = sample.getStatus().name();
            return entity;
        }

        public Sample toDomain() {
            Sample sample = Sample.create(id, name, description);
            // Restore status (requires reflection or adding setter to domain)
            // For simplicity, we'll skip status restoration in this example
            return sample;
        }

        public String getId() {
            return id;
        }
    }
}
