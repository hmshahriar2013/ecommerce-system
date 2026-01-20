package com.konasl.sampleservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Sample domain entity.
 * 
 * HEXAGONAL ARCHITECTURE TESTING:
 * - Pure unit tests, no frameworks needed
 * - No Spring context
 * - Fast and reliable
 * - Tests business logic only
 */
class SampleTest {

    @Test
    @DisplayName("Should create sample with DRAFT status")
    void shouldCreateSampleWithDraftStatus() {
        // when
        Sample sample = Sample.create("1", "Test Sample", "Description");

        // then
        assertNotNull(sample);
        assertEquals("1", sample.getId());
        assertEquals("Test Sample", sample.getName());
        assertEquals("Description", sample.getDescription());
        assertEquals(SampleStatus.DRAFT, sample.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when creating sample with blank name")
    void shouldThrowException_whenCreatingWithBlankName() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Sample.create("1", "", "Description"));

        assertEquals("Sample name cannot be blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating sample with null name")
    void shouldThrowException_whenCreatingWithNullName() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> Sample.create("1", null, "Description"));
    }

    @Test
    @DisplayName("Should activate sample when in DRAFT status")
    void shouldActivateSample_whenInDraftStatus() {
        // given
        Sample sample = Sample.create("1", "Test Sample", "Description");

        // when
        sample.activate();

        // then
        assertEquals(SampleStatus.ACTIVE, sample.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when activating already active sample")
    void shouldThrowException_whenActivatingAlreadyActiveSample() {
        // given
        Sample sample = Sample.create("1", "Test Sample", "Description");
        sample.activate();

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                sample::activate);

        assertEquals("Sample is already active", exception.getMessage());
    }

    @Test
    @DisplayName("Should deactivate active sample")
    void shouldDeactivateActiveSample() {
        // given
        Sample sample = Sample.create("1", "Test Sample", "Description");
        sample.activate();

        // when
        sample.deactivate();

        // then
        assertEquals(SampleStatus.INACTIVE, sample.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when deactivating already inactive sample")
    void shouldThrowException_whenDeactivatingAlreadyInactiveSample() {
        // given
        Sample sample = Sample.create("1", "Test Sample", "Description");
        sample.activate();
        sample.deactivate();

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                sample::deactivate);

        assertEquals("Sample is already inactive", exception.getMessage());
    }
}
