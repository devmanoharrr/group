package com.citizenscience.rewards.model;

/**
 * Enumeration describing reward badge tiers and their point thresholds.
 */
public enum BadgeLevel {
    /** No badge awarded. */
    NONE(0),
    /** Bronze badge awarded at 100 points. */
    BRONZE(100),
    /** Silver badge awarded at 200 points. */
    SILVER(200),
    /** Gold badge awarded at 500 points. */
    GOLD(500);

    /** Minimum points required to attain the badge. */
    private final int threshold;

    BadgeLevel(int threshold) {
        this.threshold = threshold;
    }

    /**
     * @return minimum point requirement for the badge
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Determines which badge applies to the supplied point total.
     *
     * @param points total accrued points
     * @return appropriate {@link BadgeLevel}
     */
    public static BadgeLevel fromPoints(int points) {
        if (points >= GOLD.threshold) {
            return GOLD;
        }
        if (points >= SILVER.threshold) {
            return SILVER;
        }
        if (points >= BRONZE.threshold) {
            return BRONZE;
        }
        return NONE;
    }
}
