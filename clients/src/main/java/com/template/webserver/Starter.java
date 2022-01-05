package com.template.webserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
@EnableCaching
public class Starter {
    /**
     * Starts our Spring Boot application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
}
