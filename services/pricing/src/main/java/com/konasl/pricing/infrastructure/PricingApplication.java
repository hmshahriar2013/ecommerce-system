package com.konasl.pricing.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot application for Pricing bounded context.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.konasl.pricing")
public class PricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingApplication.class, args);
    }
}
