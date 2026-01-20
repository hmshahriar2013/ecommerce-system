package com.konasl.useraccess.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot application for User Access bounded context.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.konasl.useraccess")
public class UserAccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAccessApplication.class, args);
    }
}
