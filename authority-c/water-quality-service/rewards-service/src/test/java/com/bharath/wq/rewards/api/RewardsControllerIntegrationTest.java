package com.bharath.wq.rewards.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.bharath.wq.rewards.model.ObservationEvent;
import com.bharath.wq.rewards.model.RewardTotals;
import java.time.Instant;
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
 * Integration tests for the RewardsController endpoints.
 *
 * <p>These tests verify the rewards calculation and retrieval functionality.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RewardsControllerIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void getTotals_shouldReturn200_withZeroPointsForNewCitizen() {
    final ResponseEntity<RewardTotals> response =
        restTemplate.getForEntity("/rewards/citizens/new-citizen-123", RewardTotals.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCitizenId()).isEqualTo("new-citizen-123");
    assertThat(response.getBody().getTotalPoints()).isEqualTo(0);
    assertThat(response.getBody().getBadges()).isEmpty();
  }

  @Test
  void ingestObservation_shouldReturn202() {
    final ObservationEvent event =
        new ObservationEvent(
            "obs-1",
            "citizen-1",
            "NE",
            12.5,
            7.2,
            95.0,
            2.1,
            Set.of("CLEAR"),
            List.of(),
            Instant.now());

    final ResponseEntity<String> response =
        restTemplate.postForEntity("/rewards/ingest", event, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  void getTotals_afterIngest_shouldHavePoints() {
    final String citizenId = "citizen-test-" + System.currentTimeMillis();

    // Ingest a complete observation (should get 20 points: 10 base + 10 bonus)
    final ObservationEvent event =
        new ObservationEvent(
            "obs-" + System.currentTimeMillis(),
            citizenId,
            "NE",
            12.5,
            7.2,
            95.0,
            2.1,
            Set.of("CLEAR"),
            List.of(),
            Instant.now());

    restTemplate.postForEntity("/rewards/ingest", event, String.class);

    // Check totals
    final ResponseEntity<RewardTotals> response =
        restTemplate.getForEntity("/rewards/citizens/" + citizenId, RewardTotals.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTotalPoints()).isEqualTo(20);
  }

  @Test
  void getTotals_afterMultipleIngests_shouldAccumulatePoints() {
    final String citizenId = "citizen-accum-" + System.currentTimeMillis();

    // Ingest 6 observations to reach 120 points (6 * 20 = 120, which qualifies for BRONZE badge)
    for (int i = 0; i < 6; i++) {
      final ObservationEvent event =
          new ObservationEvent(
              "obs-" + System.currentTimeMillis() + "-" + i,
              citizenId,
              "NE",
              12.5,
              7.2,
              95.0,
              2.1,
              Set.of("CLEAR"),
              List.of(),
              Instant.now());

      restTemplate.postForEntity("/rewards/ingest", event, String.class);
    }

    // Should have 120 points (6 * 20), which qualifies for BRONZE badge (100+)
    final ResponseEntity<RewardTotals> response =
        restTemplate.getForEntity("/rewards/citizens/" + citizenId, RewardTotals.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTotalPoints()).isEqualTo(120);
    assertThat(response.getBody().getBadges()).contains(RewardTotals.Badge.BRONZE);
  }

  @Test
  void getLeaderboard_shouldReturn200() {
    final ResponseEntity<Object> response =
        restTemplate.getForEntity("/rewards/leaderboard?authority=NE&limit=5", Object.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
