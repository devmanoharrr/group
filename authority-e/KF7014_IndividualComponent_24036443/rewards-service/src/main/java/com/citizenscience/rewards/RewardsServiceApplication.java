package com.citizenscience.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Rewards Microservice
 *
 * Hosts the REST API that reads observations from the Crowdsourced Data
 * microservice, computes point totals, and publishes badge standings. Scheduling
 * is enabled so the service can refresh cached computations when required.
 */
@SpringBootApplication
@EnableScheduling
public class RewardsServiceApplication {

    /**
     * Launches the rewards microservice.
     *
     * @param args optional Spring Boot arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RewardsServiceApplication.class, args);
    }
}
