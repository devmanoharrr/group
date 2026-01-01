package com.waterquality.data.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity class representing a water quality observation submitted by a citizen.
 * 
 * This entity stores all information related to a citizen-submitted water quality report,
 * including measurements, observations, images, and metadata.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Entity
@Table(name = "water_quality_observations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterQualityObservation {

    /**
     * Unique identifier for the observation (UUID).
     * Automatically generated upon creation.
     */
    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    /**
     * Unique identifier for the citizen who submitted the observation.
     * Required field.
     */
    @Column(name = "citizen_id", nullable = false, length = 100)
    private String citizenId;

    /**
     * Postcode where the water quality observation was made.
     * This is a mandatory field for all submissions.
     */
    @Column(name = "postcode", nullable = false, length = 10)
    private String postcode;

    /**
     * Water temperature in degrees Celsius.
     * Optional measurement field.
     */
    @Column(name = "temperature")
    private Double temperature;

    /**
     * pH level of the water sample.
     * Optional measurement field (valid range: 0-14).
     */
    @Column(name = "ph")
    private Double ph;

    /**
     * Alkalinity measurement in mg/L.
     * Optional measurement field.
     */
    @Column(name = "alkalinity")
    private Double alkalinity;

    /**
     * Turbidity measurement in NTU (Nephelometric Turbidity Units).
     * Optional measurement field.
     */
    @Column(name = "turbidity")
    private Double turbidity;

    /**
     * List of visual observations about the water quality.
     * Possible values: Clear, Cloudy, Murky, Foamy, Oily, Discoloured, Presence of Odour.
     * Stored as comma-separated string in database.
     */
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    /**
     * List of image URLs or paths (up to 3 images).
     * Stored as comma-separated string in database.
     */
    @Column(name = "images", columnDefinition = "TEXT")
    private String images;

    /**
     * Timestamp when the observation was submitted.
     * Automatically generated upon creation.
     */
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /**
     * Flag indicating whether this observation has been processed by the Rewards Service.
     * Default value is false (0).
     */
    @Builder.Default
    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    /**
     * Pre-persist callback to generate UUID before saving to database.
     */
    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.processed == null) {
            this.processed = false;
        }
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    /**
     * Checks if the observation contains all required measurements.
     * A complete record has all four measurements (temperature, pH, alkalinity, turbidity).
     * 
     * @return true if all measurements are present, false otherwise
     */
    public boolean hasAllMeasurements() {
        return temperature != null && ph != null && alkalinity != null && turbidity != null;
    }

    /**
     * Checks if the observation has at least one measurement.
     * 
     * @return true if at least one measurement is present, false otherwise
     */
    public boolean hasAnyMeasurement() {
        return temperature != null || ph != null || alkalinity != null || turbidity != null;
    }

    /**
     * Checks if the observation has visual observations.
     * 
     * @return true if observations field is not null and not empty, false otherwise
     */
    public boolean hasObservations() {
        return observations != null && !observations.trim().isEmpty();
    }

    /**
     * Checks if this is a complete record (all measurements + observations).
     * 
     * @return true if the record is complete, false otherwise
     */
    public boolean isComplete() {
        return hasAllMeasurements() && hasObservations();
    }
}
