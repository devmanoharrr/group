package com.waterquality.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Crowdsourced Data Service.
 * 
 * This is the entry point of the Spring Boot application. It configures and launches
 * the microservice for collecting citizen-submitted water quality observations.
 * 
 * The @SpringBootApplication annotation enables:
 * - @Configuration: Allows defining beans
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 * - @ComponentScan: Scans for components in this package and sub-packages
 * 
 * Features:
 * - RESTful API endpoints for data submission and retrieval
 * - SQLite database for persistent storage
 * - Request validation and error handling
 * - Comprehensive logging
 * 
 * Default port: 8081 (configured in application.properties)
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@SpringBootApplication
public class CrowdsourcedDataServiceApplication {

    /**
     * Main method to launch the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CrowdsourcedDataServiceApplication.class, args);
        
        System.out.println("\n==============================================");
        System.out.println("Crowdsourced Data Service Started Successfully");
        System.out.println("==============================================");
        System.out.println("API Base URL: http://localhost:8081/api/data");
        System.out.println("API Documentation: http://localhost:8081/swagger-ui.html");
        System.out.println("Health Check: http://localhost:8081/api/data/health");
        System.out.println("==============================================");
        System.out.println("Available APIs:");
        System.out.println("  POST   /api/data/submit");
        System.out.println("         → Submit new water quality observation");
        System.out.println("");
        System.out.println("  GET    /api/data/observations?limit=100");
        System.out.println("         → Get all observations");
        System.out.println("");
        System.out.println("  GET    /api/data/observations/{id}");
        System.out.println("         → Get observation by ID");
        System.out.println("");
        System.out.println("  GET    /api/data/citizen/{citizenId}");
        System.out.println("         → Get citizen's observations");
        System.out.println("");
        System.out.println("  GET    /api/data/stats");
        System.out.println("         → Get statistics (total/processed/unprocessed)");
        System.out.println("");
        System.out.println("  PUT    /api/data/{id}/process");
        System.out.println("         → Mark observation as processed");
        System.out.println("");
        System.out.println("  GET    /api/data/health");
        System.out.println("         → Health check");
        System.out.println("==============================================\n");
    }
}
