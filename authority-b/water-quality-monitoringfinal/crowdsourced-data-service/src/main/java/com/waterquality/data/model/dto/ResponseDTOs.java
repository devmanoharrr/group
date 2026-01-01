package com.waterquality.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTOs for the Crowdsourced Data Service API endpoints.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
public class ResponseDTOs {

    /**
     * Response DTO for successful observation submission.
     * Contains the generated ID and timestamp of the submitted observation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionResponse {
        /**
         * Status of the submission (e.g., "accepted").
         */
        private String status;

        /**
         * Generated UUID for the observation.
         */
        private String id;

        /**
         * Timestamp when the observation was created.
         */
        private LocalDateTime timestamp;

        /**
         * Success message.
         */
        private String message;
    }

    /**
     * Response DTO for observation listing.
     * Contains detailed information about a water quality observation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObservationResponse {
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
    }

    /**
     * Response DTO for statistics endpoint.
     * Provides summary statistics about submitted observations.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsResponse {
        /**
         * Total number of observations in the database.
         */
        private Long total;

        /**
         * Number of observations that have been processed by the Rewards Service.
         */
        private Long processed;

        /**
         * Number of unprocessed observations.
         */
        private Long unprocessed;
    }

    /**
     * Generic error response for API errors.
     * Provides details about validation or processing errors.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {
        /**
         * Error status (e.g., "error", "validation_failed").
         */
        private String status;

        /**
         * Error message describing what went wrong.
         */
        private String message;

        /**
         * HTTP status code.
         */
        private int code;

        /**
         * Timestamp when the error occurred.
         */
        private LocalDateTime timestamp;

        /**
         * Optional list of detailed error messages (for validation errors).
         */
        private List<String> details;
    }
}
