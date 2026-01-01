package com.citizenscience.rewards.model;

import java.util.List;

/**
 * Projection describing the reward status of a citizen.
 *
 * Encapsulates submission counts, point totals, and badge achievements that the
 * API returns to clients requesting reward information.
 */
public class RewardSummary {

    /** Identifier of the citizen whose rewards are being described. */
    private final String citizenId;
    /** Count of all valid submissions made by the citizen. */
    private final int totalSubmissions;
    /** Total points accrued including bonuses. */
    private final int totalPoints;
    /** Number of submissions qualifying for completeness bonuses. */
    private final int bonusSubmissions;
    /** Highest badge attained by the citizen. */
    private final BadgeLevel currentBadge;
    /** History of badges achieved in chronological order. */
    private final List<String> earnedBadges;

    /**
     * Builds an immutable reward summary.
     *
     * @param citizenId         identifier of the citizen
     * @param totalSubmissions  count of valid submissions
     * @param totalPoints       total points accrued
     * @param bonusSubmissions  number of complete submissions
     * @param currentBadge      highest badge currently held
     * @param earnedBadges      list of badge titles attained
     */
    public RewardSummary(String citizenId, int totalSubmissions, int totalPoints, int bonusSubmissions,
                         BadgeLevel currentBadge, List<String> earnedBadges) {
        this.citizenId = citizenId;
        this.totalSubmissions = totalSubmissions;
        this.totalPoints = totalPoints;
        this.bonusSubmissions = bonusSubmissions;
        this.currentBadge = currentBadge;
        this.earnedBadges = earnedBadges;
    }

    /**
     * @return identifier of the citizen
     */
    public String getCitizenId() {
        return citizenId;
    }

    /**
     * @return count of valid submissions
     */
    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    /**
     * @return total number of reward points
     */
    public int getTotalPoints() {
        return totalPoints;
    }

    /**
     * @return number of submissions that earned bonus points
     */
    public int getBonusSubmissions() {
        return bonusSubmissions;
    }

    /**
     * @return highest badge currently assigned
     */
    public BadgeLevel getCurrentBadge() {
        return currentBadge;
    }

    /**
     * @return list of badge names awarded to date
     */
    public List<String> getEarnedBadges() {
        return earnedBadges;
    }
}
