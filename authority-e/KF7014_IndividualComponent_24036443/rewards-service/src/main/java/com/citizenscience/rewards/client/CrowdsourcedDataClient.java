package com.citizenscience.rewards.client;

import com.citizenscience.rewards.exception.ObservationFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * HTTP client that retrieves observation records from the data microservice.
 */
@Component
public class CrowdsourcedDataClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdsourcedDataClient.class);

    /** RestTemplate used for outbound HTTP calls. */
    private final RestTemplate restTemplate;
    /** Base URL of the Crowdsourced Data microservice. */
    private final String baseUrl;

    /**
     * Creates the client with its dependencies.
     *
     * @param restTemplate Spring {@link RestTemplate} for HTTP interactions
     * @param baseUrl      root URL of the data service
     */
    public CrowdsourcedDataClient(RestTemplate restTemplate,
                                  @Value("${crowdsourced-data.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieves all observations recorded for a given citizen.
     *
     * @param citizenId identifier allocated by the data service
     * @return list of observation records, or empty when none are found
     * @throws ObservationFetchException when the downstream service is unavailable
     */
    public List<WaterObservationRecord> fetchObservationsForCitizen(String citizenId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/observations/citizen/{citizenId}")
                .buildAndExpand(citizenId)
                .toUriString();

        try {
            WaterObservationRecord[] response = restTemplate.getForObject(url, WaterObservationRecord[].class);
            if (response == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(response);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.debug("No observations found for citizen {}", citizenId);
                return Collections.emptyList();
            }
            String message = String.format("Crowdsourced data service responded with status %s", ex.getStatusCode());
            throw new ObservationFetchException(message, ex);
        } catch (ResourceAccessException ex) {
            throw new ObservationFetchException("Unable to reach the crowdsourced data service", ex);
        } catch (RestClientException ex) {
            throw new ObservationFetchException("Failed to retrieve observations from the crowdsourced data service", ex);
        }
    }
}
