package com.waterquality.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Rewards Service.
 * 
 * This is the entry point of the Spring Boot application for the Rewards microservice.
 * It manages citizen rewards, points calculation, badge awards, and leaderboards.
 * 
 * The @SpringBootApplication annotation enables:
 * - @Configuration: Allows defining beans
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 * - @ComponentScan: Scans for components in this package and sub-packages
 * 
 * Features:
 * - Processes observations from Crowdsourced Data Service
 * - Calculates and awards points (10 base + 10 bonus for complete records)
 * - Manages badges (Bronze: 100pts, Silver: 200pts, Gold: 500pts)
 * - Maintains leaderboards
 * - In-memory storage for rewards data
 * 
 * Default port: 8082 (configured in application.properties)
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@SpringBootApplication
public class RewardsServiceApplication {

    /**
     * Main method to launch the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RewardsServiceApplication.class, args);

        System.out.println("\n==========================================");
        System.out.println("Rewards Service Started Successfully");
        System.out.println("==========================================");
        System.out.println("API Base URL: http://localhost:8082/api/rewards");
        System.out.println("Health Check: http://localhost:8082/api/rewards/health");
        System.out.println("==========================================");
        System.out.println("Available APIs:");
        System.out.println("  POST   /api/rewards/process");
        System.out.println("         → Process unprocessed observations");
        System.out.println("");
        System.out.println("  GET    /api/rewards/leaderboard?top=10");
        System.out.println("         → Get top contributors ranked by points");
        System.out.println("");
        System.out.println("  GET    /api/rewards/points/{citizenId}");
        System.out.println("         → Get citizen's points and badges");
        System.out.println("");
        System.out.println("  GET    /api/rewards/badges/{citizenId}");
        System.out.println("         → Get citizen's badges");
        System.out.println("");
        System.out.println("  GET    /api/rewards/summary");
        System.out.println("         → Get rewards system summary");
        System.out.println("");
        System.out.println("  GET    /api/rewards/health");
        System.out.println("         → Health check");
        System.out.println("==========================================");
        System.out.println("Reward Rules:");
        System.out.println("  - Base submission: 10 points");
        System.out.println("  - Complete record: 20 points (10 + 10 bonus)");
        System.out.println("Badges:");
        System.out.println("  - Bronze: 100 points");
        System.out.println("  - Silver: 200 points");
        System.out.println("  - Gold: 500 points");
        System.out.println("==========================================\n");
    }
}
