package com.waterquality.rewards.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration class for Rewards Service.
 * 
 * Provides bean configurations for HTTP clients and other application components.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Configuration
public class RewardsServiceConfig {

    /**
     * Creates a RestTemplate bean for HTTP communication with other services.
     * 
     * Configured with timeouts to prevent hanging requests.
     * 
     * @param builder RestTemplateBuilder provided by Spring
     * @return configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
