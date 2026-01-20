package com.konasl.fulfillment.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot application for Fulfillment bounded context.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.konasl.fulfillment")
public class FulfillmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FulfillmentApplication.class, args);
    }
}
