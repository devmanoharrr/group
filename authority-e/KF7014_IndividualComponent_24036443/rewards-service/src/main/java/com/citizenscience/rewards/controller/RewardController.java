package com.citizenscience.rewards.controller;

import com.citizenscience.rewards.model.RewardSummary;
import com.citizenscience.rewards.service.RewardComputationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller that exposes reward lookup endpoints.
 *
 * Provides citizens and the API gateway with access to point totals, bonus
 * counts, and badge achievements derived from the crowdsourced data service.
 */
@RestController
@RequestMapping("/rewards")
public class RewardController {

    /** Service responsible for computing reward summaries. */
    private final RewardComputationService rewardComputationService;

    /**
     * Constructs the controller with its business dependency.
     *
     * @param rewardComputationService service that calculates rewards on demand
     */
    public RewardController(RewardComputationService rewardComputationService) {
        this.rewardComputationService = rewardComputationService;
    }

    /**
     * Returns the live reward summary for a single citizen.
     *
     * <p>Example response:</p>
     * <pre>
     * {
     *   "citizenId": "CTZ-001",
     *   "totalPoints": 20,
     *   "currentBadge": "NONE"
     * }
     * </pre>
     *
     * @param citizenId identifier allocated by the data service
     * @return reward summary with totals and badges
     */
    @GetMapping("/{citizenId}")
    public ResponseEntity<RewardSummary> getRewardsForCitizen(@PathVariable @NotBlank String citizenId) {
        return ResponseEntity.ok(rewardComputationService.calculateRewardsForCitizen(citizenId));
    }

    /**
     * Lists all cached summaries currently stored in memory.
     *
     * @return list of cached reward summaries
     */
    @GetMapping
    public ResponseEntity<List<RewardSummary>> getCachedRewards() {
        return ResponseEntity.ok(rewardComputationService.getCachedSummaries());
    }
}
