package com.citizenscience.rewards.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.citizenscience.rewards.exception.ObservationFetchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class CrowdsourcedDataClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private CrowdsourcedDataClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        client = new CrowdsourcedDataClient(restTemplate, "http://localhost:8081");
    }

    @Test
    void fetchObservationsForCitizenIgnoresUnknownFields() {
        String responseBody = "[" +
                "{\"id\":\"obs-1\",\"citizenId\":\"citizen-001\",\"postcode\":\"NE1 4LP\"," +
                "\"temperature\":12.5,\"ph\":7.1,\"complete\":false," +
                "\"observations\":[\"CLEAR\"]," +
                "\"measurements\":{\"temperature\":12.5,\"ph\":7.1}" +
                "}]";

        mockServer.expect(requestTo("http://localhost:8081/observations/citizen/citizen-001"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<WaterObservationRecord> results = client.fetchObservationsForCitizen("citizen-001");

        mockServer.verify();

        assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(record -> {
                    assertThat(record.getCitizenId()).isEqualTo("citizen-001");
                    assertThat(record.getTemperature()).isEqualTo(12.5);
                    assertThat(record.getPh()).isEqualTo(7.1);
                });
    }

    @Test
    void fetchObservationsReturnsEmptyListWhenNotFound() {
        mockServer.expect(requestTo("http://localhost:8081/observations/citizen/citizen-404"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        List<WaterObservationRecord> results = client.fetchObservationsForCitizen("citizen-404");

        assertThat(results).isEmpty();
    }

    @Test
    void fetchObservationsThrowsCustomExceptionOnServerError() {
        mockServer.expect(requestTo("http://localhost:8081/observations/citizen/citizen-500"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.fetchObservationsForCitizen("citizen-500"))
                .isInstanceOf(ObservationFetchException.class)
                .hasMessageContaining("status 500 INTERNAL_SERVER_ERROR");
    }
}
