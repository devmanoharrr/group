package com.waterquality.rewards.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Objects for Rewards Service.
 * 
 * These DTOs are used for API requests/responses and communication
 * with the Crowdsourced Data Service.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
public class RewardsDTOs {

    /**
     * DTO for observation data received from Data Service.
     * Mirrors the structure from Crowdsourced Data Service.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObservationDTO {
        private String id;
        private String citizenId;
        private String postcode;
        private Double temperature;
        private Double ph;
        private Double alkalinity;
        private Double turbidity;
        private List<String> observations;
        private List<String> images;
        private LocalDateTime timestamp;
        private Boolean processed;

        /**
         * Checks if this observation is complete (all measurements + observations).
         * Complete records earn bonus points.
         * 
         * @return true if all fields are present
         */
        public boolean isComplete() {
            boolean allMeasurements = temperature != null && ph != null && 
                                     alkalinity != null && turbidity != null;
            boolean hasObservations = observations != null && !observations.isEmpty();
            return allMeasurements && hasObservations;
        }
    }

    /**
     * Response DTO for process rewards endpoint.
     * Shows how many observations were processed.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessResponse {
        private Integer processedCount;
        private String message;
        private LocalDateTime timestamp;
    }

    /**
     * DTO for leaderboard entries.
     * Contains citizen info, points, and badges.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaderboardEntry {
        private String citizenId;
        private Integer points;
        private List<String> badges;
        private Integer rank;
    }

    /**
     * Response DTO for leaderboard endpoint.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaderboardResponse {
        private List<LeaderboardEntry> leaderboard;
        private Integer totalCitizens;
    }

    /**
     * Response DTO for citizen points lookup.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CitizenRewardResponse {
        private String citizenId;
        private Integer points;
        private List<String> badges;
        private String tier;
    }

    /**
     * Generic error response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {
        private String status;
        private String message;
        private Integer code;
        private LocalDateTime timestamp;
    }
}
