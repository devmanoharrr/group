package citizen.adapter;

import citizen.CitizenScienceWaterQualityApplication;
import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.repository.CrowdsourcedRepository;
import citizen.rewards.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests for Authority-A Adapter Controller
 * Tests the standardized API contract endpoints
 */
@SpringBootTest(classes = CitizenScienceWaterQualityApplication.class, 
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AdapterControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CrowdsourcedRepository repository;

    @Autowired
    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        // Ensure we have some test data
        if (repository.count() == 0) {
            CrowdsourcedRecord record = new CrowdsourcedRecord();
            record.setCitizenId("test-user");
            record.setPostcode("SW1A 1AA");
            record.setTemperature(18.5);
            record.setpH(7.2);
            record.setAlkalinity(120.0);
            record.setTurbidity(1.5);
            record.setObservations("Clear");
            repository.save(record);
            rewardService.processAllNow();
        }
    }

    @Test
    void testGetObservationCount_Returns200() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/observations/count", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("count");
        assertThat(response.getBody().get("count")).isInstanceOf(Number.class);
    }

    @Test
    void testGetRecentObservations_Returns200() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                "/api/observations/recent?limit=5", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(List.class);
    }

    @Test
    void testGetRecentObservations_ValidatesLimit() {
        // Test invalid limit (too high)
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/observations/recent?limit=100", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    void testGetLeaderboard_Returns200() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                "/api/rewards/leaderboard?limit=3", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(List.class);
    }

    @Test
    void testGetLeaderboard_ValidatesLimit() {
        // Test invalid limit (too high)
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/rewards/leaderboard?limit=100", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
    }
}

