package com.waterquality.rewards.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.waterquality.rewards.model.dto.RewardsDTOs;
import com.waterquality.rewards.service.RewardsService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for the Rewards Service.
 * 
 * This controller provides endpoints for:
 * - Processing observations and awarding rewards
 * - Retrieving leaderboards
 * - Getting citizen-specific reward information
 * 
 * Base path: /api/rewards
 * 
 * Endpoints:
 * - POST   /api/rewards/process                : Process unprocessed observations
 * - GET    /api/rewards/leaderboard            : Get top contributors
 * - GET    /api/rewards/points/{citizenId}     : Get citizen's points and badges
 * - GET    /api/rewards/health                 : Health check
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RewardsController {

    private final RewardsService rewardsService;

    /**
     * Processes unprocessed observations and awards points/badges.
     * 
     * This endpoint:
     * 1. Fetches unprocessed observations from Data Service
     * 2. Calculates and awards points
     * 3. Updates badges
     * 4. Marks observations as processed
     * 
     * @return ResponseEntity with process response (200 OK)
     * 
     * Example Request:
     * POST /api/rewards/process
     * 
     * Example Response:
     * {
     *   "processedCount": 5,
     *   "message": "Successfully processed 5 observations",
     *   "timestamp": "2025-11-11T03:30:00"
     * }
     */
    @PostMapping("/process")
    public ResponseEntity<RewardsDTOs.ProcessResponse> processRewards() {
        log.info("Received request to process rewards");

        RewardsDTOs.ProcessResponse response = rewardsService.processObservations();

        return ResponseEntity.ok(response);
    }

    /**
     * Gets the leaderboard of top contributors.
     * 
     * Returns citizens ranked by points, with their badges.
     * 
     * @param top number of top contributors to return (default: 10)
     * @return ResponseEntity with leaderboard (200 OK)
     * 
     * Example Request:
     * GET /api/rewards/leaderboard?top=10
     * 
     * Example Response:
     * {
     *   "leaderboard": [
     *     {
     *       "citizenId": "user123",
     *       "points": 150,
     *       "badges": ["Bronze", "Silver"],
     *       "rank": 1
     *     }
     *   ],
     *   "totalCitizens": 25
     * }
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<RewardsDTOs.LeaderboardResponse> getLeaderboard(
            @RequestParam(defaultValue = "10") int top) {

        log.info("Fetching leaderboard, top {}", top);

        // Validate top parameter
        if (top < 1) {
            top = 10;
        } else if (top > 100) {
            top = 100; // Maximum limit
        }

        RewardsDTOs.LeaderboardResponse leaderboard = rewardsService.getLeaderboard(top);

        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Gets reward information for a specific citizen.
     * 
     * Returns the citizen's total points, earned badges, and tier.
     * 
     * @param citizenId the citizen's unique identifier
     * @return ResponseEntity with citizen rewards (200 OK)
     * 
     * Example Request:
     * GET /api/rewards/points/user123
     * 
     * Example Response:
     * {
     *   "citizenId": "user123",
     *   "points": 150,
     *   "badges": ["Bronze", "Silver"],
     *   "tier": "Silver"
     * }
     */
    @GetMapping("/points/{citizenId}")
    public ResponseEntity<RewardsDTOs.CitizenRewardResponse> getCitizenPoints(
            @PathVariable String citizenId) {

        log.info("Fetching rewards for citizen: {}", citizenId);

        RewardsDTOs.CitizenRewardResponse rewards = rewardsService.getCitizenRewards(citizenId);

        return ResponseEntity.ok(rewards);
    }

    /**
     * Alternative endpoint for getting badges specifically.
     * 
     * @param citizenId the citizen's unique identifier
     * @return ResponseEntity with citizen rewards (200 OK)
     */
    @GetMapping("/badges/{citizenId}")
    public ResponseEntity<RewardsDTOs.CitizenRewardResponse> getCitizenBadges(
            @PathVariable String citizenId) {

        log.info("Fetching badges for citizen: {}", citizenId);

        RewardsDTOs.CitizenRewardResponse rewards = rewardsService.getCitizenRewards(citizenId);

        return ResponseEntity.ok(rewards);
    }

    /**
     * Health check endpoint.
     * 
     * @return ResponseEntity with status message (200 OK)
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Health check requested");
        return ResponseEntity.ok("Rewards Service is running");
    }

    /**
     * Gets summary of rewards system.
     * 
     * @return ResponseEntity with summary information
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        log.info("Fetching rewards summary");

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCitizens", rewardsService.getAllCitizens().size());
        summary.put("activeRewardsProgram", true);
        summary.put("badgeTiers", List.of("Bronze: 100pts", "Silver: 200pts", "Gold: 500pts"));

        return ResponseEntity.ok(summary);
    }

    /**
     * Global exception handler for this controller.
     * 
     * @param ex the exception
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RewardsDTOs.ErrorResponse> handleException(Exception ex) {
        log.error("Error in Rewards Controller", ex);

        RewardsDTOs.ErrorResponse errorResponse = RewardsDTOs.ErrorResponse.builder()
                .status("error")
                .message(ex.getMessage())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
