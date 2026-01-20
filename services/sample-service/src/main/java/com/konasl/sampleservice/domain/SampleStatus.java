package com.konasl.sampleservice.domain;

/**
 * Domain value object representing the status of a Sample.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - Pure Java enum with NO framework dependencies
 * - Represents business concepts and states
 */
public enum SampleStatus {
    DRAFT,
    ACTIVE,
    INACTIVE
}
