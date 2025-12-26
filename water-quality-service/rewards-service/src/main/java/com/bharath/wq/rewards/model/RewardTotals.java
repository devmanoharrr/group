package com.bharath.wq.rewards.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the total rewards (points and badges) for a citizen.
 *
 * <p>This class tracks accumulated points and automatically awards badges based on point
 * thresholds: Bronze (100), Silver (200), Gold (500).
 */
public class RewardTotals {
  public enum Badge {
    BRONZE,
    SILVER,
    GOLD
  }

  private final String citizenId;
  private long totalPoints;
  private final Set<Badge> badges = EnumSet.noneOf(Badge.class);

  /**
   * Constructs a new RewardTotals for a citizen.
   *
   * @param citizenId the citizen ID
   */
  public RewardTotals(String citizenId) {
    this.citizenId = citizenId;
  }

  /**
   * Jackson constructor for deserialization.
   *
   * @param citizenId the citizen ID
   * @param totalPoints the total points
   * @param badges the badges (ignored - recalculated from points)
   */
  @JsonCreator
  public RewardTotals(
      @JsonProperty("citizenId") String citizenId,
      @JsonProperty("totalPoints") long totalPoints,
      @JsonProperty("badges") Set<Badge> badges) {
    this.citizenId = citizenId;
    this.totalPoints = totalPoints;
    // Always recalculate badges based on points to ensure consistency
    recalculateBadges();
  }

  // --- getters for JSON serialization ---
  public String getCitizenId() {
    return citizenId;
  }

  public long getTotalPoints() {
    return totalPoints;
  }

  public Set<Badge> getBadges() {
    return badges;
  }

  // --- domain logic ---
  public void addPoints(long pts) {
    this.totalPoints += pts;
    recalculateBadges();
  }

  /** Recalculates badges based on current total points. */
  private void recalculateBadges() {
    badges.clear();
    if (totalPoints >= 500) {
      badges.add(Badge.GOLD);
    }
    if (totalPoints >= 200) {
      badges.add(Badge.SILVER);
    }
    if (totalPoints >= 100) {
      badges.add(Badge.BRONZE);
    }
  }
}
