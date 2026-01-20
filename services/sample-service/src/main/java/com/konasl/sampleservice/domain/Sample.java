package com.konasl.sampleservice.domain;

/**
 * Domain entity representing a Sample in the business domain.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is a pure Java class with NO framework dependencies
 * - NO Spring annotations
 * - NO JPA annotations
 * - NO persistence concerns
 * - Contains only business logic and domain rules
 */
public class Sample {

    private String id;
    private String name;
    private String description;
    private SampleStatus status;

    // Private constructor for domain integrity
    private Sample(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = SampleStatus.DRAFT;
    }

    /**
     * Factory method to create a new Sample.
     * This encapsulates domain logic for entity creation.
     */
    public static Sample create(String id, String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Sample name cannot be blank");
        }
        return new Sample(id, name, description);
    }

    /**
     * Domain behavior: Activate the sample
     */
    public void activate() {
        if (this.status == SampleStatus.ACTIVE) {
            throw new IllegalStateException("Sample is already active");
        }
        this.status = SampleStatus.ACTIVE;
    }

    /**
     * Domain behavior: Deactivate the sample
     */
    public void deactivate() {
        if (this.status == SampleStatus.INACTIVE) {
            throw new IllegalStateException("Sample is already inactive");
        }
        this.status = SampleStatus.INACTIVE;
    }

    // Getters (no setters to enforce immutability and encapsulation)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SampleStatus getStatus() {
        return status;
    }
}
