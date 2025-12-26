package com.bharath.wq.rewards.service;

import com.bharath.wq.rewards.model.RewardTotals;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RewardsStore {
  // citizenId -> totals
  private final Map<String, RewardTotals> totals = new ConcurrentHashMap<>();
  // processed observation ids (idempotency)
  private final Set<String> seen = ConcurrentHashMap.newKeySet();
  // authority -> citizenId -> points (for leaderboard)
  private final Map<String, Map<String, Long>> authorityPoints = new ConcurrentHashMap<>();

  public boolean hasSeen(String obsId) {
    return seen.contains(obsId);
  }

  public void markSeen(String obsId) {
    seen.add(obsId);
  }

  public RewardTotals totalsFor(String citizenId) {
    return totals.computeIfAbsent(citizenId, RewardTotals::new);
  }

  public void addAuthorityPoints(String authority, String citizenId, long delta) {
    if (authority == null || authority.isBlank()) {
      return;
    }
    authorityPoints
        .computeIfAbsent(authority, a -> new ConcurrentHashMap<>())
        .merge(citizenId, delta, Long::sum);
  }

  public List<Map.Entry<String, Long>> topN(String authority, int n) {
    final var m = authorityPoints.getOrDefault(authority, Map.of());
    return m.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(n)
        .collect(Collectors.toList());
  }
}
