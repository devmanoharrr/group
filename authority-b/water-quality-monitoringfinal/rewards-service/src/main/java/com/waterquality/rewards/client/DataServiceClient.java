package com.waterquality.rewards.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.waterquality.rewards.model.dto.RewardsDTOs;

import java.util.List;

/**
 * Client for communicating with the Crowdsourced Data Service.
 * 
 * This client handles all HTTP communication with the Data Service,
 * including fetching observations and marking them as processed.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataServiceClient {

    private final RestTemplate restTemplate;

    @Value("${data.service.url:http://localhost:8081}")
    private String dataServiceUrl;

    /**
     * Fetches all observations from the Data Service.
     * 
     * @return list of observations
     */
    public List<RewardsDTOs.ObservationDTO> getAllObservations() {
        String url = dataServiceUrl + "/api/data/observations";
        log.info("Fetching observations from: {}", url);

        try {
            ResponseEntity<List<RewardsDTOs.ObservationDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RewardsDTOs.ObservationDTO>>() {}
            );

            List<RewardsDTOs.ObservationDTO> observations = response.getBody();
            log.info("Retrieved {} observations from Data Service", 
                    observations != null ? observations.size() : 0);

            return observations;
        } catch (Exception e) {
            log.error("Error fetching observations from Data Service", e);
            throw new RuntimeException("Failed to fetch observations: " + e.getMessage());
        }
    }

    /**
     * Fetches unprocessed observations from the Data Service.
     * 
     * @return list of unprocessed observations
     */
    public List<RewardsDTOs.ObservationDTO> getUnprocessedObservations() {
        List<RewardsDTOs.ObservationDTO> allObservations = getAllObservations();
        
        // Filter for unprocessed observations
        List<RewardsDTOs.ObservationDTO> unprocessed = allObservations.stream()
                .filter(obs -> obs.getProcessed() != null && !obs.getProcessed())
                .toList();

        log.info("Found {} unprocessed observations", unprocessed.size());
        return unprocessed;
    }

    /**
     * Marks an observation as processed in the Data Service.
     * 
     * @param observationId the observation ID to mark as processed
     */
    public void markAsProcessed(String observationId) {
        String url = dataServiceUrl + "/api/data/" + observationId + "/process";
        log.info("Marking observation as processed: {}", observationId);

        try {
            restTemplate.put(url, null);
            log.info("Successfully marked observation {} as processed", observationId);
        } catch (Exception e) {
            log.error("Error marking observation as processed: {}", observationId, e);
            throw new RuntimeException("Failed to mark observation as processed: " + e.getMessage());
        }
    }

    /**
     * Tests connectivity to the Data Service.
     * 
     * @return true if Data Service is reachable
     */
    public boolean isDataServiceAvailable() {
        String url = dataServiceUrl + "/api/data/health";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Data Service is not available at {}", dataServiceUrl);
            return false;
        }
    }
}
