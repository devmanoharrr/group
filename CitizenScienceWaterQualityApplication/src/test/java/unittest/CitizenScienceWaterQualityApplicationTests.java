package unittest;

import citizen.CitizenScienceWaterQualityApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import citizen.crowdsourced.dto.CrowdsourcedRequest;
import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.service.CrowdsourcedService;
import citizen.rewards.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CitizenScienceWaterQualityApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CitizenScienceWaterQualityApplicationTests {
    @Autowired
    private CrowdsourcedService crowdsourcedService;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private TestRestTemplate restTemplate;
    
    public CitizenScienceWaterQualityApplicationTests() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }
    @Test
    void contextLoads() {
        assertThat(crowdsourcedService).isNotNull();
        assertThat(rewardService).isNotNull();
    }

    @Test
    void submitValidData() {
        CrowdsourcedRequest req = new CrowdsourcedRequest();
        req.setCitizenId("CITZ4488");
        req.setPostcode("450065");
        req.setTemperature(24.5);
        req.setObservations(List.of("Clear"));
        CrowdsourcedRecord record = crowdsourcedService.submit(req);
        assertThat(record.getId()).isNotNull();
        assertThat(record.getPostcode()).isEqualTo("450065");
    }

    @Test
    void rewardProcessingPostData() {
        CrowdsourcedRequest req = new CrowdsourcedRequest();
        req.setCitizenId("CITZ7898");
        req.setPostcode("400001");
        req.setTemperature(22.0);
        req.setPh(7.0);
        req.setObservations(List.of("Clear", "Foamy"));
        crowdsourcedService.submit(req);

        rewardService.processAllNow();
        Map<String, Object> summary = rewardService.getSummary("CITZ7898");

        assertThat(summary.get("points")).isInstanceOf(Integer.class);
    }

    @Test
    void gatewaySubmitPostData() {
        String url = "/api/gateway/submit";
        CrowdsourcedRequest req = new CrowdsourcedRequest();
        req.setCitizenId("CITZ2121");
        req.setPostcode("400001");
        req.setTemperature(23.0);
        req.setObservations(List.of("Clear"));

        HttpEntity<CrowdsourcedRequest> entity = new HttpEntity<>(req);
        ResponseEntity<CrowdsourcedRecord> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, CrowdsourcedRecord.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCitizenId()).isEqualTo("CITZ2121");
    }
}
