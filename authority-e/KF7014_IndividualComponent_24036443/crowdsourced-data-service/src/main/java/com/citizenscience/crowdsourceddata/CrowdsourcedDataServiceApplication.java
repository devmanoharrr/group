package com.citizenscience.crowdsourceddata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Crowdsourced Data Microservice
 *
 * Boots the Spring context that accepts citizen submissions, validates payloads,
 * persists observations, and exposes REST endpoints consumed by the rewards
 * pipeline. Requests reach this service either directly or through the API
 * gateway.
 */
@SpringBootApplication
public class CrowdsourcedDataServiceApplication {

    /**
     * Launches the Crowdsourced Data microservice.
     *
     * @param args optional Spring Boot startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CrowdsourcedDataServiceApplication.class, args);
    }
}
