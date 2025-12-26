package com.waterquality.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for API Gateway routing.
 * 
 * This class defines the routes using Java code as an alternative to YAML configuration.
 * It provides programmatic control over routing logic and allows for custom filters.
 * 
 * Routes:
 * - /data/** → Crowdsourced Data Service (localhost:8081)
 * - /rewards/** → Rewards Service (localhost:8082)
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Configuration
public class GatewayConfig {

    /**
     * Configures routes programmatically as an alternative to YAML.
     * 
     * This bean is optional - routes are already defined in application.yml.
     * Keeping this here as a reference for programmatic configuration.
     * 
     * @param builder RouteLocatorBuilder provided by Spring Cloud Gateway
     * @return configured RouteLocator
     */
    // @Bean
    // public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    //     return builder.routes()
    //             // Route to Data Service
    //             .route("data-service", r -> r
    //                     .path("/data/**")
    //                     .filters(f -> f.rewritePath("/data/(?<segment>.*)", "/api/data/${segment}"))
    //                     .uri("http://localhost:8081"))
    //             
    //             // Route to Rewards Service
    //             .route("rewards-service", r -> r
    //                     .path("/rewards/**")
    //                     .filters(f -> f.rewritePath("/rewards/(?<segment>.*)", "/api/rewards/${segment}"))
    //                     .uri("http://localhost:8082"))
    //             .build();
    // }

    /**
     * Note: The above code is commented out because we're using YAML configuration
     * in application.yml. Both approaches work - YAML is simpler for this use case.
     * 
     * To use programmatic configuration instead:
     * 1. Uncomment the @Bean and method above
     * 2. Comment out the routes section in application.yml
     */
}
