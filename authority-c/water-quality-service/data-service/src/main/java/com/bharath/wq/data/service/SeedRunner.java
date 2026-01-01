package com.bharath.wq.data.service;

import com.bharath.wq.data.model.ObservationTag;
import com.bharath.wq.data.repo.ObservationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedRunner implements CommandLineRunner {

  private final ObservationRepository repo;
  private final boolean enabled;

  public SeedRunner(ObservationRepository repo, @Value("${wq.seed.enabled:true}") boolean enabled) {
    this.repo = repo;
    this.enabled = enabled;
  }

  @Override
  public void run(String... args) {
    if (!enabled) {
      return;
    }
    if (repo.countByAuthority(null) > 0) {
      return;
    }
    
    final var baseTime = Instant.now();
    
    // User1 - 5 complete observations (100 points)
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user1", "NE1 4LP", 18.5, 7.2, 120.0, 1.5, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(864000)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user1", "NE1 4LQ", 19.0, 7.3, 125.0, 1.8, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(777600)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user1", "NE1 4LR", 17.8, 7.1, 115.0, 1.2, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(691200)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user1", "NE1 4LS", 20.1, 7.4, 130.0, 2.0, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(604800)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user1", "NE1 4LT", 18.2, 7.2, 118.0, 1.6, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(518400)));

    // User2 - 4 complete (80 points) + 2 incomplete (20 points) = 100 points
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user2", "NE2 2AB", 16.5, 6.8, 95.0, 2.5, Set.of(ObservationTag.CLOUDY), List.of(), "NE", baseTime.minusSeconds(432000)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user2", "NE2 2AC", 17.0, 6.9, 100.0, 2.8, Set.of(ObservationTag.CLOUDY, ObservationTag.FOAMY), List.of(), "NE", baseTime.minusSeconds(345600)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user2", "NE2 2AD", 15.8, 6.7, 90.0, 3.0, Set.of(ObservationTag.MURKY), List.of(), "NE", baseTime.minusSeconds(259200)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user2", "NE2 2AE", 16.2, 6.9, 98.0, 2.6, Set.of(ObservationTag.FOAMY), List.of(), "NE", baseTime.minusSeconds(172800)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user2", "NE2 2AF", null, 6.5, null, 4.4, Set.of(ObservationTag.ODOUR), List.of(), "NE", baseTime.minusSeconds(86400)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user2", "NE2 2AG", 16.0, null, null, 2.2, Set.of(ObservationTag.DISCOLOURED), List.of(), "NE", baseTime.minusSeconds(43200)));

    // User3 - 3 complete (60 points) + 1 incomplete (10 points) = 70 points
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user3", "NE3 3XY", 14.5, 7.0, 85.0, 1.8, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(950400)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user3", "NE3 3XZ", 15.0, 7.1, 88.0, 1.9, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(864000)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user3", "NE3 3YA", 14.8, 6.9, 87.0, 1.7, Set.of(ObservationTag.CLEAR), List.of(), "NE", baseTime.minusSeconds(777600)));
    repo.insert(new ObservationRecord(UUID.randomUUID().toString(), "user3", "NE3 3YB", null, 6.8, null, null, Set.of(ObservationTag.ODOUR), List.of(), "NE", baseTime.minusSeconds(21600)));
  }
}
