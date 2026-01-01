package com.citizenscience.crowdsourceddata.model;

import com.citizenscience.crowdsourceddata.util.StringListConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Entity describing an individual water quality observation submitted by a citizen.
 *
 * Captures the postcode, quantitative measurements, qualitative observations, and
 * optional imagery for use by the rewards service and downstream analytics.
 */
@Entity
@Table(name = "water_observation")
public class WaterObservation {

    /** Unique UUID used as the primary key for the record. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /** Identifier of the citizen who submitted the observation. */
    @Column(name = "citizen_id", nullable = false)
    private String citizenId;

    /** Postcode describing the location of the sampled water. */
    @Column(name = "postcode", nullable = false)
    private String postcode;

    /** Temperature measurement in degrees Celsius, if provided. */
    @Column(name = "temperature")
    private Double temperature;

    /** pH measurement describing acidity of the sample. */
    @Column(name = "ph")
    private Double ph;

    /** Alkalinity measurement in milligrams per litre. */
    @Column(name = "alkalinity")
    private Double alkalinity;

    /** Turbidity measurement in NTU reflecting water clarity. */
    @Column(name = "turbidity")
    private Double turbidity;

    /** Timestamp recorded when the submission was stored. */
    @Column(name = "submission_timestamp", nullable = false)
    private LocalDateTime submissionTimestamp;

    /** Flag indicating whether the submission satisfied completeness rules. */
    @Column(name = "is_complete", nullable = false)
    private boolean complete;

    /** Collection of qualitative observations describing water appearance. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "water_observation_conditions", joinColumns = @JoinColumn(name = "water_observation_id"))
    @Column(name = "condition")
    @Enumerated(EnumType.STRING)
    private Set<ObservationCondition> conditions = new HashSet<>();

    /** Base64 encoded images represented as strings for SQLite compatibility. */
    @Convert(converter = StringListConverter.class)
    @Column(name = "images", columnDefinition = "TEXT")
    private List<String> images = new ArrayList<>();

    /**
     * Creates a new observation with a generated UUID.
     */
    public WaterObservation() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * @return unique identifier for the observation
     */
    public String getId() {
        return id;
    }

    /**
     * @return citizen identifier linked to the observation
     */
    public String getCitizenId() {
        return citizenId;
    }

    /**
     * Associates the observation with a citizen.
     *
     * @param citizenId sequential identifier of the submitting citizen
     */
    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    /**
     * @return postcode provided by the citizen
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Stores the postcode describing the observation location.
     *
     * @param postcode trimmed postcode string
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * @return recorded temperature value or {@code null} when absent
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Updates the optional temperature measurement.
     *
     * @param temperature temperature in degrees Celsius
     */
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /**
     * @return recorded pH value or {@code null} when absent
     */
    public Double getPh() {
        return ph;
    }

    /**
     * Updates the optional pH measurement.
     *
     * @param ph acidity level of the sample
     */
    public void setPh(Double ph) {
        this.ph = ph;
    }

    /**
     * @return recorded alkalinity value or {@code null} when absent
     */
    public Double getAlkalinity() {
        return alkalinity;
    }

    /**
     * Updates the optional alkalinity measurement.
     *
     * @param alkalinity concentration expressed in mg/L
     */
    public void setAlkalinity(Double alkalinity) {
        this.alkalinity = alkalinity;
    }

    /**
     * @return recorded turbidity value or {@code null} when absent
     */
    public Double getTurbidity() {
        return turbidity;
    }

    /**
     * Updates the optional turbidity measurement.
     *
     * @param turbidity NTU value representing cloudiness
     */
    public void setTurbidity(Double turbidity) {
        this.turbidity = turbidity;
    }

    /**
     * @return timestamp captured at submission time
     */
    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    /**
     * Sets the timestamp recorded when the observation was saved.
     *
     * @param submissionTimestamp moment of persistence
     */
    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    /**
     * @return {@code true} when the observation satisfies completeness criteria
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Updates the completeness flag used by the rewards logic.
     *
     * @param complete whether all data points were supplied
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * @return qualitative conditions associated with the observation
     */
    public Set<ObservationCondition> getConditions() {
        return conditions;
    }

    /**
     * Sets the collection of qualitative observation conditions.
     *
     * @param conditions enumerated values describing the sample
     */
    public void setConditions(Set<ObservationCondition> conditions) {
        this.conditions = conditions;
    }

    /**
     * @return list of Base64 encoded images captured during sampling
     */
    public List<String> getImages() {
        return images;
    }

    /**
     * Replaces the stored image payloads with a defensive copy.
     *
     * @param images Base64 encoded image strings
     */
    public void setImages(List<String> images) {
        this.images = images == null ? new ArrayList<>() : new ArrayList<>(images);
    }
}
