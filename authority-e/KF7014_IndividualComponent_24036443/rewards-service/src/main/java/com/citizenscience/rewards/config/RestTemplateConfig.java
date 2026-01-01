package com.citizenscience.rewards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration providing HTTP client infrastructure for the rewards service.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates the {@link RestTemplate} used to communicate with the data service.
     *
     * @return configured {@link RestTemplate} instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
