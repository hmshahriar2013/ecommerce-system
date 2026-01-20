package com.konasl.sampleservice.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application Entry Point - INFRASTRUCTURE LAYER.
 * 
 * HEXAGONAL ARCHITECTURE RULES:
 * - This is INFRASTRUCTURE/BOOTSTRAPPING code
 * - Spring Boot annotations are appropriate here
 * - Scans all packages to wire adapters and use cases
 * - Starts the application framework
 */
@SpringBootApplication(scanBasePackages = "com.konasl.sampleservice")
public class SampleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleServiceApplication.class, args);
    }
}
