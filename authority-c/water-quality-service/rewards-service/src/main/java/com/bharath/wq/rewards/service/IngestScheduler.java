package com.bharath.wq.rewards.service;

import com.bharath.wq.rewards.model.ObservationDTO;
import com.bharath.wq.rewards.model.ObservationEvent;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IngestScheduler {

  private final DataClient client;
  private final RewardsService rewards;
  private volatile Instant lastSeen = Instant.EPOCH;

  private final String authorityFilter; // optional ('' means all)

  public IngestScheduler(
      DataClient client,
      RewardsService rewards,
      @Value("${rewards.authority-filter:}") String authorityFilter) {
    this.client = client;
    this.rewards = rewards;
    this.authorityFilter = authorityFilter == null ? "" : authorityFilter.trim().toUpperCase();
  }

  @Scheduled(fixedDelayString = "${rewards.poll.ms:5000}")
  public void poll() {
    final List<ObservationDTO> batch = client.fetchLatest();
    if (batch == null || batch.isEmpty()) {
      return;
    }

    // filter by optional authority and createdAt strictly newer than lastSeen
    final List<ObservationDTO> news =
        batch.stream()
            .filter(
                o ->
                    authorityFilter.isEmpty()
                        || (o.authority() != null
                            && authorityFilter.equals(o.authority().trim().toUpperCase())))
            .filter(o -> o.createdAt() != null && o.createdAt().isAfter(lastSeen))
            .sorted(Comparator.comparing(ObservationDTO::createdAt)) // oldest->newest
            .collect(Collectors.toList());

    if (news.isEmpty()) {
      return;
    }

    for (final ObservationDTO o : news) {
      rewards.ingest(toEvent(o));
      if (o.createdAt().isAfter(lastSeen)) {
        lastSeen = o.createdAt();
      }
    }
  }

  private static ObservationEvent toEvent(ObservationDTO o) {
    // observations come as ENUM strings; pass them through as-is
    final Set<String> obs = o.observations();
    return new ObservationEvent(
        o.id(),
        o.citizenId(),
        o.authority(),
        o.temperatureC(),
        o.pH(),
        o.alkalinityMgL(),
        o.turbidityNTU(),
        obs,
        o.imagePaths(),
        o.createdAt());
  }
}
