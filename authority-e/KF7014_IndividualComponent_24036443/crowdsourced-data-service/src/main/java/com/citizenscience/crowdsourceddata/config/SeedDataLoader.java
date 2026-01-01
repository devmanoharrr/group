package com.citizenscience.crowdsourceddata.config;

import com.citizenscience.crowdsourceddata.dto.WaterObservationRequest;
import com.citizenscience.crowdsourceddata.model.WaterObservation;
import com.citizenscience.crowdsourceddata.repository.WaterObservationRepository;
import com.citizenscience.crowdsourceddata.service.WaterObservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Seed data loader for Authority-E
 * Creates demo observations and registers citizens on startup
 */
@Component
@Slf4j
public class SeedDataLoader implements CommandLineRunner {

    private final WaterObservationRepository repository;
    private final WaterObservationService service;

    public SeedDataLoader(WaterObservationRepository repository, WaterObservationService service) {
        this.repository = repository;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (repository.count() > 0) {
            log.info("Database already contains data. Skipping seed data.");
            return;
        }

        log.info("Loading seed data for Authority-E...");

        List<WaterObservationRequest> seedRequests = Arrays.asList(
            // User1 - 5 complete observations (will register as CTZ-001, then use that ID)
            createRequest("NE1 4LP", 18.5, 7.2, 120.0, 1.5, Arrays.asList("Clear")),
            createRequest("NE1 4LQ", 19.0, 7.3, 125.0, 1.8, Arrays.asList("Clear")),
            createRequest("NE1 4LR", 17.8, 7.1, 115.0, 1.2, Arrays.asList("Clear")),
            createRequest("NE1 4LS", 20.1, 7.4, 130.0, 2.0, Arrays.asList("Clear")),
            createRequest("NE1 4LT", 18.2, 7.2, 118.0, 1.6, Arrays.asList("Clear")),

            // User2 - 4 complete + 2 incomplete
            createRequest("NE2 2AB", 16.5, 6.8, 95.0, 2.5, Arrays.asList("Cloudy")),
            createRequest("NE2 2AC", 17.0, 6.9, 100.0, 2.8, Arrays.asList("Cloudy", "Foamy")),
            createRequest("NE2 2AD", 15.8, 6.7, 90.0, 3.0, Arrays.asList("Murky")),
            createRequest("NE2 2AE", 16.2, 6.9, 98.0, 2.6, Arrays.asList("Foamy")),
            createRequest("NE2 2AF", null, 6.5, null, null, Arrays.asList("Presence of Odour")),
            createRequest("NE2 2AG", 16.0, null, null, 2.2, Arrays.asList("Discoloured")),

            // User3 - 3 complete + 1 incomplete
            createRequest("NE3 3XY", 14.5, 7.0, 85.0, 1.8, Arrays.asList("Clear")),
            createRequest("NE3 3XZ", 15.0, 7.1, 88.0, 1.9, Arrays.asList("Clear")),
            createRequest("NE3 3YA", 14.8, 6.9, 87.0, 1.7, Arrays.asList("Clear")),
            createRequest("NE3 3YB", null, 6.8, null, null, Arrays.asList("Presence of Odour"))
        );

        String user1Id = null;
        String user2Id = null;
        String user3Id = null;
        
        for (int i = 0; i < seedRequests.size(); i++) {
            WaterObservationRequest request = seedRequests.get(i);
            try {
                if (i == 0) {
                    // First request registers user1
                    var response = service.saveObservationForNewCitizen(request);
                    user1Id = response.getCitizenId();
                    log.info("Registered first citizen: {}", user1Id);
                } else if (i < 5) {
                    // Next 4 for user1
                    service.saveObservation(user1Id, request);
                } else if (i == 5) {
                    // First request for user2 registers new citizen
                    var response = service.saveObservationForNewCitizen(request);
                    user2Id = response.getCitizenId();
                    log.info("Registered second citizen: {}", user2Id);
                } else if (i < 11) {
                    // Next 5 for user2
                    service.saveObservation(user2Id, request);
                } else if (i == 11) {
                    // First request for user3 registers new citizen
                    var response = service.saveObservationForNewCitizen(request);
                    user3Id = response.getCitizenId();
                    log.info("Registered third citizen: {}", user3Id);
                } else {
                    // Remaining for user3
                    service.saveObservation(user3Id, request);
                }
            } catch (Exception e) {
                log.error("Failed to seed observation {}: {}", i, e.getMessage());
            }
        }

        log.info("Seed data loaded successfully. {} observations created.", seedRequests.size());
    }

    private WaterObservationRequest createRequest(String postcode,
                                                 Double temperature, Double ph, Double alkalinity, Double turbidity,
                                                 List<String> observations) {
        WaterObservationRequest request = new WaterObservationRequest();
        request.setPostcode(postcode);
        request.setTemperature(temperature);
        request.setPh(ph);
        request.setAlkalinity(alkalinity);
        request.setTurbidity(turbidity);
        request.setObservations(observations);
        return request;
    }
}

