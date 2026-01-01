package com.citizenscience.rewards.service;

import com.citizenscience.rewards.client.CrowdsourcedDataClient;
import com.citizenscience.rewards.client.WaterObservationRecord;
import com.citizenscience.rewards.exception.ObservationFetchException;
import com.citizenscience.rewards.model.BadgeLevel;
import com.citizenscience.rewards.model.RewardSummary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Calculates reward points and badge levels for citizens by consuming the
 * Crowdsourced Data microservice.
 *
 * Maintains an in-memory cache of summaries that refresh on a schedule so the
 * API can respond quickly even if the downstream service is temporarily
 * unavailable.
 */
@Service
public class RewardComputationService {

    private static final int BASE_POINTS_PER_SUBMISSION = 10;
    private static final int BONUS_POINTS_PER_COMPLETE_SUBMISSION = 10;

    /** Client used to fetch observations from the data service. */
    private final CrowdsourcedDataClient dataClient;
    /** Cache of computed reward summaries keyed by citizen ID. */
    private final Map<String, RewardSummary> cachedRewards = new ConcurrentHashMap<>();

    /**
     * Constructs the service with the required HTTP client dependency.
     *
     * @param dataClient REST client for observation retrieval
     */
    public RewardComputationService(CrowdsourcedDataClient dataClient) {
        this.dataClient = dataClient;
    }

    /**
     * Computes reward information for a citizen, caching the result for reuse.
     *
     * @param citizenId identifier of the citizen
     * @return up-to-date reward summary
     * @throws ObservationFetchException when the data service cannot be reached
     */
    public RewardSummary calculateRewardsForCitizen(String citizenId) {
        try {
            List<WaterObservationRecord> records = dataClient.fetchObservationsForCitizen(citizenId);
            RewardSummary summary = computeSummary(citizenId, records);
            cachedRewards.put(citizenId, summary);
            return summary;
        } catch (ObservationFetchException ex) {
            RewardSummary cached = cachedRewards.get(citizenId);
            if (cached != null) {
                return cached;
            }
            throw ex;
        }
    }

    /**
     * Refreshes all cached summaries using the latest data from the source service.
     */
    @Scheduled(fixedDelayString = "${rewards.refresh-interval-ms:60000}")
    public void refreshCachedRewards() {
        cachedRewards.replaceAll((citizenId, summary) -> {
            try {
                List<WaterObservationRecord> records = dataClient.fetchObservationsForCitizen(citizenId);
                return computeSummary(citizenId, records);
            } catch (ObservationFetchException ex) {
                return summary;
            }
        });
    }

    /**
     * Exposes the current cache primarily for diagnostics and testing.
     *
     * @return snapshot list of cached reward summaries
     */
    public List<RewardSummary> getCachedSummaries() {
        return new ArrayList<>(cachedRewards.values());
    }

    private RewardSummary computeSummary(String citizenId, List<WaterObservationRecord> records) {
        int totalSubmissions = records.size();
        int bonusSubmissions = (int) records.stream().filter(WaterObservationRecord::isComplete).count();
        int basePoints = totalSubmissions * BASE_POINTS_PER_SUBMISSION;
        int bonusPoints = bonusSubmissions * BONUS_POINTS_PER_COMPLETE_SUBMISSION;
        int totalPoints = basePoints + bonusPoints;

        BadgeLevel currentBadge = BadgeLevel.fromPoints(totalPoints);
        List<String> earnedBadges = List.of(BadgeLevel.BRONZE, BadgeLevel.SILVER, BadgeLevel.GOLD).stream()
                .filter(badge -> totalPoints >= badge.getThreshold())
                .sorted(Comparator.comparingInt(BadgeLevel::getThreshold))
                .map(Enum::name)
                .collect(Collectors.toList());

        return new RewardSummary(citizenId, totalSubmissions, totalPoints, bonusSubmissions, currentBadge, earnedBadges);
    }
}
