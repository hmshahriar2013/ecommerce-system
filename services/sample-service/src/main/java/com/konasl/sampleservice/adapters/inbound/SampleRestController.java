package com.konasl.sampleservice.adapters.inbound;

import com.konasl.sampleservice.application.CreateSampleUseCase;
import com.konasl.sampleservice.application.ActivateSampleUseCase;
import com.konasl.sampleservice.domain.Sample;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller - INBOUND ADAPTER for HTTP requests.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is an ADAPTER, not part of the core domain
 * - Spring annotations are ONLY allowed here (adapter layer)
 * - Controller is THIN and delegates to use cases
 * - Handles HTTP concerns: request/response mapping, status codes
 * - Depends inward on application layer (use cases)
 */
@RestController
@RequestMapping("/api/samples")
public class SampleRestController {

    private final CreateSampleUseCase createSampleUseCase;
    private final ActivateSampleUseCase activateSampleUseCase;

    public SampleRestController(
            CreateSampleUseCase createSampleUseCase,
            ActivateSampleUseCase activateSampleUseCase) {
        this.createSampleUseCase = createSampleUseCase;
        this.activateSampleUseCase = activateSampleUseCase;
    }

    @PostMapping
    public ResponseEntity<SampleResponse> createSample(@RequestBody CreateSampleRequest request) {
        Sample sample = createSampleUseCase.execute(
                request.id(),
                request.name(),
                request.description());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SampleResponse.fromDomain(sample));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<SampleResponse> activateSample(@PathVariable String id) {
        Sample sample = activateSampleUseCase.execute(id);

        return ResponseEntity
                .ok(SampleResponse.fromDomain(sample));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Sample service is running");
    }
}
