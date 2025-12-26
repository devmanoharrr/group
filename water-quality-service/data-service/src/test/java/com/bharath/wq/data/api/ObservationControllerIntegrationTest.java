package com.bharath.wq.data.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.bharath.wq.data.api.dto.CreateObservationRequest;
import com.bharath.wq.data.api.dto.CreateObservationResult;
import com.bharath.wq.data.api.dto.ObservationResponse;
import com.bharath.wq.data.model.ObservationTag;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for the ObservationController endpoints.
 *
 * <p>These tests verify the full request/response cycle including database persistence.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ObservationControllerIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createObservation_shouldReturn201AndLocation() throws Exception {
    final CreateObservationRequest request =
        new CreateObservationRequest(
            "citizen-123",
            "NE1 4LP",
            12.5,
            7.2,
            95.0,
            2.1,
            Set.of(ObservationTag.CLEAR),
            List.of("images/test.jpg"),
            "NE");

    final ResponseEntity<CreateObservationResult> response =
        restTemplate.postForEntity("/observations", request, CreateObservationResult.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders().getLocation()).isNotNull();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotBlank();
  }

  @Test
  void getObservationById_shouldReturn200() throws Exception {
    // First create an observation
    final CreateObservationRequest request =
        new CreateObservationRequest(
            "citizen-456",
            "NE2 2AB",
            null,
            7.5,
            null,
            null,
            Set.of(ObservationTag.MURKY),
            null,
            "NE");

    final ResponseEntity<CreateObservationResult> createResponse =
        restTemplate.postForEntity("/observations", request, CreateObservationResult.class);

    final String observationId = createResponse.getBody().id();

    // Then retrieve it
    final ResponseEntity<ObservationResponse> getResponse =
        restTemplate.getForEntity("/observations/" + observationId, ObservationResponse.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody()).isNotNull();
    assertThat(getResponse.getBody().id()).isEqualTo(observationId);
    assertThat(getResponse.getBody().citizenId()).isEqualTo("citizen-456");
    assertThat(getResponse.getBody().postcode()).isEqualTo("NE2 2AB");
  }

  @Test
  void getObservationById_shouldReturn404_whenNotFound() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("/observations/non-existent-id", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getLatestObservations_shouldReturn200() {
    final ResponseEntity<ObservationResponse[]> response =
        restTemplate.getForEntity("/observations/latest?limit=5", ObservationResponse[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void getLatestObservations_withAuthority_shouldReturnFiltered() {
    final ResponseEntity<ObservationResponse[]> response =
        restTemplate.getForEntity(
            "/observations/latest?authority=NE&limit=10", ObservationResponse[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void getCount_shouldReturn200() {
    final ResponseEntity<Long> response =
        restTemplate.getForEntity("/observations/count", Long.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void createObservation_shouldReturn400_whenInvalidPostcode() {
    final CreateObservationRequest request =
        new CreateObservationRequest(
            "citizen-789",
            "INVALID",
            12.5,
            null,
            null,
            null,
            Set.of(ObservationTag.CLEAR),
            null,
            "NE");

    final ResponseEntity<String> response =
        restTemplate.postForEntity("/observations", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createObservation_shouldReturn400_whenNoMeasurementsOrObservations() {
    final CreateObservationRequest request =
        new CreateObservationRequest(
            "citizen-999", "NE1 4LP", null, null, null, null, null, null, "NE");

    final ResponseEntity<String> response =
        restTemplate.postForEntity("/observations", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
