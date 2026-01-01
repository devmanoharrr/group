package com.citizenscience.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Microservice
 *
 * Starts the Spring Cloud Gateway instance that forwards citizen submissions to
 * the Crowdsourced Data microservice and reward lookups to the Rewards
 * microservice. It exposes a unified entry point on port 8080.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Launches the API gateway.
     *
     * @param args optional Spring Boot arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
