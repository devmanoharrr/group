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
    final var now = Instant.now();
    repo.insert(
        new ObservationRecord(
            UUID.randomUUID().toString(),
            "alice",
            "NE1 4LP",
            12.3,
            7.2,
            95.0,
            2.1,
            Set.of(ObservationTag.CLEAR),
            List.of("images/tyne1.jpg"),
            "NE",
            now));

    repo.insert(
        new ObservationRecord(
            UUID.randomUUID().toString(),
            "bob",
            "NE2 2AB",
            null,
            null,
            null,
            4.4,
            Set.of(ObservationTag.MURKY, ObservationTag.ODOUR),
            List.of("images/tyne2.png"),
            "NE",
            now));
  }
}
