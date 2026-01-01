package citizen.config;

import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.repository.CrowdsourcedRepository;
import citizen.rewards.service.RewardService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Seed data loader for Authority-A
 * Creates demo observations and processes rewards on startup
 */
@Component
public class SeedDataLoader {

    private final CrowdsourcedRepository repository;
    private final RewardService rewardService;

    @Autowired
    public SeedDataLoader(CrowdsourcedRepository repository, RewardService rewardService) {
        this.repository = repository;
        this.rewardService = rewardService;
    }

    @PostConstruct
    public void loadSeedData() {
        // Only seed if database is empty
        if (repository.count() > 0) {
            return;
        }

        List<CrowdsourcedRecord> seedRecords = Arrays.asList(
            // User1 - 5 complete observations (will get 20 points each = 100 points)
            createRecord("user1", "SW1A 1AA", 18.5, 7.2, 120.0, 1.5, "Clear water, good quality", LocalDateTime.now().minusDays(10)),
            createRecord("user1", "SW1A 1AB", 19.0, 7.3, 125.0, 1.8, "Excellent condition", LocalDateTime.now().minusDays(9)),
            createRecord("user1", "SW1A 1AC", 17.8, 7.1, 115.0, 1.2, "Very clear", LocalDateTime.now().minusDays(8)),
            createRecord("user1", "SW1A 1AD", 20.1, 7.4, 130.0, 2.0, "Perfect quality", LocalDateTime.now().minusDays(7)),
            createRecord("user1", "SW1A 1AE", 18.2, 7.2, 118.0, 1.6, "Good condition", LocalDateTime.now().minusDays(6)),

            // User2 - 4 complete observations (80 points) + 2 incomplete (20 points) = 100 points
            createRecord("user2", "NW1 6XE", 16.5, 6.8, 95.0, 2.5, "Slight discoloration", LocalDateTime.now().minusDays(5)),
            createRecord("user2", "NW1 6XF", 17.0, 6.9, 100.0, 2.8, "Cloudy appearance", LocalDateTime.now().minusDays(4)),
            createRecord("user2", "NW1 6XG", 15.8, 6.7, 90.0, 3.0, "Murky water", LocalDateTime.now().minusDays(3)),
            createRecord("user2", "NW1 6XH", 16.2, 6.9, 98.0, 2.6, "Foamy surface", LocalDateTime.now().minusDays(2)),
            createRecord("user2", "NW1 6XI", null, 6.5, null, null, "Presence of odour", LocalDateTime.now().minusDays(1)),
            createRecord("user2", "NW1 6XJ", 16.0, null, null, 2.2, "Discoloured", LocalDateTime.now().minusHours(12)),

            // User3 - 3 complete observations (60 points) + 1 incomplete (10 points) = 70 points
            createRecord("user3", "E1 6AN", 14.5, 7.0, 85.0, 1.8, "Clear", LocalDateTime.now().minusDays(11)),
            createRecord("user3", "E1 6AO", 15.0, 7.1, 88.0, 1.9, "Good quality", LocalDateTime.now().minusDays(10)),
            createRecord("user3", "E1 6AP", 14.8, 6.9, 87.0, 1.7, "Acceptable", LocalDateTime.now().minusDays(9)),
            createRecord("user3", "E1 6AQ", null, 6.8, null, null, "Slight odour", LocalDateTime.now().minusHours(6))
        );

        repository.saveAll(seedRecords);

        // Process rewards for all records
        rewardService.processAllNow();
    }

    private CrowdsourcedRecord createRecord(String citizenId, String postcode, 
                                           Double temperature, Double pH, Double alkalinity, Double turbidity,
                                           String observations, LocalDateTime timestamp) {
        CrowdsourcedRecord record = new CrowdsourcedRecord();
        record.setCitizenId(citizenId);
        record.setPostcode(postcode);
        record.setTemperature(temperature);
        record.setpH(pH);
        record.setAlkalinity(alkalinity);
        record.setTurbidity(turbidity);
        // Observations is stored as a String (comma-separated or single value)
        record.setObservations(observations);
        record.setTimestamp(timestamp);
        return record;
    }
}

