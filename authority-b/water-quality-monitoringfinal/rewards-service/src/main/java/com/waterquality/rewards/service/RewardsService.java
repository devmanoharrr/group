package com.waterquality.rewards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.waterquality.rewards.client.DataServiceClient;
import com.waterquality.rewards.model.dto.RewardsDTOs;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing citizen rewards, points, and badges.
 * 
 * This service maintains in-memory storage of points and badges,
 * processes observations to award points, and provides leaderboard functionality.
 * 
 * Reward Rules:
 * - Base submission: 10 points
 * - Complete record (all measurements + observations): +10 bonus = 20 total points
 * 
 * Badge Thresholds:
 * - Bronze: 100 points
 * - Silver: 200 points
 * - Gold: 500 points
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardsService {

    private final DataServiceClient dataServiceClient;

    /**
     * In-memory storage for citizen points.
     * Key: citizenId, Value: total points
     */
    private final Map<String, Integer> pointsStore = new ConcurrentHashMap<>();

    /**
     * In-memory storage for citizen badges.
     * Key: citizenId, Value: list of badges
     */
    private final Map<String, List<String>> badgesStore = new ConcurrentHashMap<>();

    /**
     * Points awarded for a basic valid submission.
     */
    private static final int BASE_POINTS = 10;

    /**
     * Bonus points for a complete record (all measurements + observations).
     */
    private static final int COMPLETE_RECORD_BONUS = 10;

    /**
     * Badge thresholds: name → points required
     */
    private static final Map<String, Integer> BADGE_THRESHOLDS = Map.of(
            "Bronze", 100,
            "Silver", 200,
            "Gold", 500
    );

    /**
     * Processes unprocessed observations and awards points/badges.
     * 
     * This method:
     * 1. Fetches unprocessed observations from Data Service
     * 2. Calculates points for each observation
     * 3. Updates citizen's total points
     * 4. Awards appropriate badges
     * 5. Marks observation as processed in Data Service
     * 
     * @return response with count of processed observations
     */
    public RewardsDTOs.ProcessResponse processObservations() {
        log.info("Starting rewards processing...");

        List<RewardsDTOs.ObservationDTO> unprocessed = dataServiceClient.getUnprocessedObservations();

        if (unprocessed.isEmpty()) {
            log.info("No unprocessed observations found");
            return RewardsDTOs.ProcessResponse.builder()
                    .processedCount(0)
                    .message("No unprocessed observations to process")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        int processedCount = 0;

        for (RewardsDTOs.ObservationDTO observation : unprocessed) {
            try {
                // Calculate points for this observation
                int points = calculatePoints(observation);

                // Award points to citizen
                String citizenId = observation.getCitizenId();
                int currentPoints = pointsStore.getOrDefault(citizenId, 0);
                int newTotal = currentPoints + points;
                pointsStore.put(citizenId, newTotal);

                // Update badges based on new total
                updateBadges(citizenId, newTotal);

                // Mark as processed in Data Service
                dataServiceClient.markAsProcessed(observation.getId());

                processedCount++;
                log.info("Processed observation {} for citizen {}. Awarded {} points. New total: {}",
                        observation.getId(), citizenId, points, newTotal);

            } catch (Exception e) {
                log.error("Error processing observation {}", observation.getId(), e);
                // Continue processing other observations
            }
        }

        log.info("Rewards processing complete. Processed {} observations", processedCount);

        return RewardsDTOs.ProcessResponse.builder()
                .processedCount(processedCount)
                .message("Successfully processed " + processedCount + " observations")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Calculates points for an observation.
     * 
     * @param observation the observation to evaluate
     * @return points earned (10 for basic, 20 for complete)
     */
    private int calculatePoints(RewardsDTOs.ObservationDTO observation) {
        int points = BASE_POINTS;

        // Check if record is complete for bonus points
        if (observation.isComplete()) {
            points += COMPLETE_RECORD_BONUS;
            log.debug("Observation {} is complete. Awarding bonus points", observation.getId());
        }

        return points;
    }

    /**
     * Updates badges for a citizen based on their total points.
     * 
     * @param citizenId the citizen ID
     * @param totalPoints the citizen's total points
     */
    private void updateBadges(String citizenId, int totalPoints) {
        List<String> badges = new ArrayList<>();

        // Award all badges that the citizen qualifies for
        if (totalPoints >= BADGE_THRESHOLDS.get("Gold")) {
            badges.add("Gold");
            badges.add("Silver");
            badges.add("Bronze");
        } else if (totalPoints >= BADGE_THRESHOLDS.get("Silver")) {
            badges.add("Silver");
            badges.add("Bronze");
        } else if (totalPoints >= BADGE_THRESHOLDS.get("Bronze")) {
            badges.add("Bronze");
        }

        badgesStore.put(citizenId, badges);

        if (!badges.isEmpty()) {
            log.info("Citizen {} has earned badges: {}", citizenId, badges);
        }
    }

    /**
     * Gets the leaderboard of top contributors.
     * 
     * @param limit maximum number of entries to return
     * @return leaderboard response with ranked citizens
     */
    public RewardsDTOs.LeaderboardResponse getLeaderboard(int limit) {
        log.info("Fetching leaderboard, top {}", limit);

        List<RewardsDTOs.LeaderboardEntry> entries = pointsStore.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    int rank = new ArrayList<>(pointsStore.values()).indexOf(entry.getValue()) + 1;
                    return RewardsDTOs.LeaderboardEntry.builder()
                            .citizenId(entry.getKey())
                            .points(entry.getValue())
                            .badges(badgesStore.getOrDefault(entry.getKey(), Collections.emptyList()))
                            .rank(rank)
                            .build();
                })
                .collect(Collectors.toList());

        // Add proper ranking
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return RewardsDTOs.LeaderboardResponse.builder()
                .leaderboard(entries)
                .totalCitizens(pointsStore.size())
                .build();
    }

    /**
     * Gets reward information for a specific citizen.
     * 
     * @param citizenId the citizen ID
     * @return citizen reward response with points and badges
     */
    public RewardsDTOs.CitizenRewardResponse getCitizenRewards(String citizenId) {
        log.info("Fetching rewards for citizen: {}", citizenId);

        int points = pointsStore.getOrDefault(citizenId, 0);
        List<String> badges = badgesStore.getOrDefault(citizenId, Collections.emptyList());

        // Determine tier
        String tier = determineTier(badges);

        return RewardsDTOs.CitizenRewardResponse.builder()
                .citizenId(citizenId)
                .points(points)
                .badges(badges)
                .tier(tier)
                .build();
    }

    /**
     * Determines the tier based on badges.
     * 
     * @param badges list of badges
     * @return tier name
     */
    private String determineTier(List<String> badges) {
        if (badges.contains("Gold")) return "Gold";
        if (badges.contains("Silver")) return "Silver";
        if (badges.contains("Bronze")) return "Bronze";
        return "Beginner";
    }

    /**
     * Gets all citizen IDs that have earned points.
     * 
     * @return set of citizen IDs
     */
    public Set<String> getAllCitizens() {
        return new HashSet<>(pointsStore.keySet());
    }

    /**
     * Clears all rewards data (for testing purposes).
     */
    public void clearAllData() {
        pointsStore.clear();
        badgesStore.clear();
        log.warn("All rewards data has been cleared");
    }
}
