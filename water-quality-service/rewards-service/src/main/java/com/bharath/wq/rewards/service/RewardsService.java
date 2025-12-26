package com.bharath.wq.rewards.service;

import com.bharath.wq.rewards.model.LeaderboardRow;
import com.bharath.wq.rewards.model.ObservationEvent;
import com.bharath.wq.rewards.model.RewardTotals;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service layer for managing citizen rewards and points.
 *
 * <p>This service handles the business logic for calculating points, awarding badges, and
 * maintaining leaderboards based on observation submissions.
 */
@Service
public class RewardsService {

  private static final Logger log = LoggerFactory.getLogger(RewardsService.class);
  private static final long BASE_POINTS = 10;
  private static final long BONUS_COMPLETE = 10;

  private final RewardsStore store;

  /**
   * Constructs a new RewardsService.
   *
   * @param store the rewards store for data persistence
   */
  public RewardsService(RewardsStore store) {
    this.store = store;
  }

  /**
   * Ingests an observation event and calculates rewards.
   *
   * <p>Awards 10 base points for any valid observation, plus 10 bonus points if the observation is
   * complete (has all measurements and at least one observation tag). Idempotent - duplicate
   * observations are ignored.
   *
   * @param ev the observation event to process
   */
  public void ingest(ObservationEvent ev) {
    if (ev == null || ev.id() == null || ev.id().isBlank()) {
      log.debug("Skipping invalid observation event");
      return;
    }
    if (store.hasSeen(ev.id())) {
      log.debug("Observation {} already processed, skipping", ev.id());
      return; // idempotent
    }

    long pts = BASE_POINTS;
    if (isComplete(ev)) {
      pts += BONUS_COMPLETE;
      log.debug("Awarding {} points (base + bonus) for complete observation {}", pts, ev.id());
    } else {
      log.debug("Awarding {} points (base) for observation {}", pts, ev.id());
    }

    final RewardTotals rt = store.totalsFor(ev.citizenId());
    rt.addPoints(pts);
    store.addAuthorityPoints(normalize(ev.authority()), ev.citizenId(), pts);
    store.markSeen(ev.id());
    log.info(
        "Processed observation {} for citizen {}, total points: {}",
        ev.id(),
        ev.citizenId(),
        rt.getTotalPoints());
  }

  /**
   * Retrieves the reward totals for a citizen.
   *
   * @param citizenId the citizen ID
   * @return the reward totals including points and badges
   */
  public RewardTotals getTotals(String citizenId) {
    return store.totalsFor(citizenId);
  }

  /**
   * Retrieves the leaderboard for a specific authority.
   *
   * @param authority the authority code (e.g., "NE")
   * @param limit maximum number of entries to return (clamped between 1 and 50)
   * @return list of leaderboard rows ordered by points (highest first)
   */
  public List<LeaderboardRow> leaderboard(String authority, int limit) {
    final int safe = Math.max(1, Math.min(limit, 50));
    return store.topN(normalize(authority), safe).stream()
        .map(e -> new LeaderboardRow(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  private static boolean isComplete(ObservationEvent ev) {
    final boolean allMeasurements =
        ev.temperatureC() != null
            && ev.pH() != null
            && ev.alkalinityMgL() != null
            && ev.turbidityNTU() != null;
    final Set<String> obs = ev.observations();
    final boolean hasObservation = obs != null && !obs.isEmpty();
    return allMeasurements && hasObservation;
  }

  private static String normalize(String auth) {
    if (auth == null) {
      return null;
    }
    final String t = auth.trim().toUpperCase();
    return t.isEmpty() ? null : t;
  }
}
