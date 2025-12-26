package com.waterquality.data.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for water quality observation submission requests.
 * 
 * This class represents the payload sent by citizens when submitting a water quality observation.
 * It includes validation constraints to ensure data integrity before processing.
 * 
 * Validation Rules:
 * - Postcode is mandatory (not null or blank)
 * - At least one measurement OR one observation must be provided (validated in service layer)
 * - Maximum 3 images allowed (validated in service layer)
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRequest {

    /**
     * Unique identifier for the citizen submitting the observation.
     * This field is required and cannot be blank.
     */
    @NotBlank(message = "Citizen ID is required")
    private String citizenId;

    /**
     * Postcode where the observation was made.
     * This field is mandatory for all submissions.
     */
    @NotBlank(message = "Postcode is required")
    private String postcode;

    /**
     * Measurements object containing water quality measurements.
     * Contains temperature, pH, alkalinity, and turbidity (all optional).
     */
    private Measurements measurements;

    /**
     * List of visual observations about the water quality.
     * Valid values: Clear, Cloudy, Murky, Foamy, Oily, Discoloured, Presence of Odour.
     * At least one observation or one measurement is required.
     */
    private List<String> observations;

    /**
     * List of image URLs or file paths (maximum 3).
     * Optional field for photographic evidence.
     */
    private List<String> images;

    /**
     * Nested class for water quality measurements.
     * All fields are optional, but at least one measurement or observation is required.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Measurements {
        /**
         * Water temperature in degrees Celsius.
         */
        private Double temperature;

        /**
         * pH level (0-14 scale).
         */
        private Double ph;

        /**
         * Alkalinity in mg/L.
         */
        private Double alkalinity;

        /**
         * Turbidity in NTU (Nephelometric Turbidity Units).
         */
        private Double turbidity;

        /**
         * Checks if any measurement value is present.
         * 
         * @return true if at least one measurement is not null, false otherwise
         */
        public boolean hasAnyValue() {
            return temperature != null || ph != null || alkalinity != null || turbidity != null;
        }
    }
}
