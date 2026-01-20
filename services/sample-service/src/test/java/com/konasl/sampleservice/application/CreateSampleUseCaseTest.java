package com.konasl.sampleservice.application;

import com.konasl.sampleservice.domain.Sample;
import com.konasl.sampleservice.domain.SampleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateSampleUseCase.
 * 
 * HEXAGONAL ARCHITECTURE TESTING:
 * - Test application layer with mocked ports
 * - No Spring context needed
 * - Verifies orchestration logic
 */
@ExtendWith(MockitoExtension.class)
class CreateSampleUseCaseTest {

    @Mock
    private SampleRepository sampleRepository;

    private CreateSampleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSampleUseCase(sampleRepository);
    }

    @Test
    @DisplayName("Should create and save sample")
    void shouldCreateAndSaveSample() {
        // given
        String id = "123";
        String name = "Test Sample";
        String description = "Test Description";

        Sample expectedSample = Sample.create(id, name, description);
        when(sampleRepository.save(any(Sample.class))).thenReturn(expectedSample);

        // when
        Sample result = useCase.execute(id, name, description);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(SampleStatus.DRAFT, result.getStatus());

        verify(sampleRepository).save(any(Sample.class));
    }

    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowException_whenNameIsBlank() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("123", "", "Description"));

        verify(sampleRepository, never()).save(any());
    }
}
