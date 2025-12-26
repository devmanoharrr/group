package com.waterquality.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the API Gateway.
 * 
 * This is the entry point of the Spring Cloud Gateway application.
 * It provides a unified entry point for all microservices in the
 * Water Quality Monitoring system.
 * 
 * The API Gateway routes requests to:
 * - Crowdsourced Data Service (port 8081)
 * - Rewards Service (port 8082)
 * 
 * Features:
 * - Request routing based on path patterns
 * - Load balancing (future enhancement)
 * - Cross-cutting concerns (logging, security)
 * - Simplified client access
 * 
 * Default port: 8080 (configured in application.yml)
 * 
 * Architecture:
 * Client → API Gateway (8080) → Data Service (8081)
 *                              → Rewards Service (8082)
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Main method to launch the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);

        System.out.println("\n==============================================");
        System.out.println("API Gateway Started Successfully");
        System.out.println("==============================================");
        System.out.println("Gateway URL: http://localhost:8080");
        System.out.println("==============================================");
        System.out.println("Routing Configuration:");
        System.out.println("");
        System.out.println("  /data/**");
        System.out.println("  → Crowdsourced Data Service (Port 8081)");
        System.out.println("     Examples:");
        System.out.println("     POST   http://localhost:8080/data/submit");
        System.out.println("     GET    http://localhost:8080/data/observations");
        System.out.println("     GET    http://localhost:8080/data/stats");
        System.out.println("");
        System.out.println("  /rewards/**");
        System.out.println("  → Rewards Service (Port 8082)");
        System.out.println("     Examples:");
        System.out.println("     POST   http://localhost:8080/rewards/process");
        System.out.println("     GET    http://localhost:8080/rewards/leaderboard");
        System.out.println("     GET    http://localhost:8080/rewards/points/{id}");
        System.out.println("==============================================");
        System.out.println("Health Check: http://localhost:8080/actuator/health");
        System.out.println("==============================================\n");
    }
}
