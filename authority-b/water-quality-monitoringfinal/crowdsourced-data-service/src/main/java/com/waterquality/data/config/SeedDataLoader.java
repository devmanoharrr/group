package com.waterquality.data.config;

import com.waterquality.data.model.dto.SubmissionRequest;
import com.waterquality.data.model.entity.WaterQualityObservation;
import com.waterquality.data.repository.WaterQualityObservationRepository;
import com.waterquality.data.service.WaterQualityObservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Seed data loader for Authority-B
 * Creates demo observations on startup if database is empty
 */
@Component
@Slf4j
public class SeedDataLoader implements CommandLineRunner {

    private final WaterQualityObservationRepository repository;
    private final WaterQualityObservationService service;

    public SeedDataLoader(WaterQualityObservationRepository repository, 
                         WaterQualityObservationService service) {
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

        log.info("Loading seed data for Authority-B...");

        List<SubmissionRequest> seedRequests = Arrays.asList(
            // CTZ-001 - 5 complete observations (100 points)
            createRequest("CTZ-001", "NE1 4LP", 18.5, 7.2, 120.0, 1.5, Arrays.asList("Clear"), LocalDateTime.now().minusDays(10)),
            createRequest("CTZ-001", "NE1 4LQ", 19.0, 7.3, 125.0, 1.8, Arrays.asList("Clear"), LocalDateTime.now().minusDays(9)),
            createRequest("CTZ-001", "NE1 4LR", 17.8, 7.1, 115.0, 1.2, Arrays.asList("Clear"), LocalDateTime.now().minusDays(8)),
            createRequest("CTZ-001", "NE1 4LS", 20.1, 7.4, 130.0, 2.0, Arrays.asList("Clear"), LocalDateTime.now().minusDays(7)),
            createRequest("CTZ-001", "NE1 4LT", 18.2, 7.2, 118.0, 1.6, Arrays.asList("Clear"), LocalDateTime.now().minusDays(6)),

            // CTZ-002 - 4 complete (80 points) + 2 incomplete (20 points) = 100 points
            createRequest("CTZ-002", "NE2 2AB", 16.5, 6.8, 95.0, 2.5, Arrays.asList("Cloudy"), LocalDateTime.now().minusDays(5)),
            createRequest("CTZ-002", "NE2 2AC", 17.0, 6.9, 100.0, 2.8, Arrays.asList("Cloudy", "Foamy"), LocalDateTime.now().minusDays(4)),
            createRequest("CTZ-002", "NE2 2AD", 15.8, 6.7, 90.0, 3.0, Arrays.asList("Murky"), LocalDateTime.now().minusDays(3)),
            createRequest("CTZ-002", "NE2 2AE", 16.2, 6.9, 98.0, 2.6, Arrays.asList("Foamy"), LocalDateTime.now().minusDays(2)),
            createRequest("CTZ-002", "NE2 2AF", null, 6.5, null, null, Arrays.asList("Presence of Odour"), LocalDateTime.now().minusDays(1)),
            createRequest("CTZ-002", "NE2 2AG", 16.0, null, null, 2.2, Arrays.asList("Discoloured"), LocalDateTime.now().minusHours(12)),

            // CTZ-003 - 3 complete (60 points) + 1 incomplete (10 points) = 70 points
            createRequest("CTZ-003", "NE3 3XY", 14.5, 7.0, 85.0, 1.8, Arrays.asList("Clear"), LocalDateTime.now().minusDays(11)),
            createRequest("CTZ-003", "NE3 3XZ", 15.0, 7.1, 88.0, 1.9, Arrays.asList("Clear"), LocalDateTime.now().minusDays(10)),
            createRequest("CTZ-003", "NE3 3YA", 14.8, 6.9, 87.0, 1.7, Arrays.asList("Clear"), LocalDateTime.now().minusDays(9)),
            createRequest("CTZ-003", "NE3 3YB", null, 6.8, null, null, Arrays.asList("Presence of Odour"), LocalDateTime.now().minusHours(6))
        );

        for (SubmissionRequest request : seedRequests) {
            try {
                service.submitObservation(request);
            } catch (Exception e) {
                log.error("Failed to seed observation: {}", e.getMessage());
            }
        }

        log.info("Seed data loaded successfully. {} observations created.", seedRequests.size());
    }

    private SubmissionRequest createRequest(String citizenId, String postcode,
                                           Double temperature, Double ph, Double alkalinity, Double turbidity,
                                           List<String> observations, LocalDateTime timestamp) {
        SubmissionRequest.Measurements measurements = SubmissionRequest.Measurements.builder()
                .temperature(temperature)
                .ph(ph)
                .alkalinity(alkalinity)
                .turbidity(turbidity)
                .build();

        SubmissionRequest request = SubmissionRequest.builder()
                .citizenId(citizenId)
                .postcode(postcode)
                .measurements(measurements)
                .observations(observations)
                .build();

        // Note: Timestamp is set automatically by the entity via @CreationTimestamp
        // The service will use current time, which is fine for demo purposes
        return request;
    }
}

