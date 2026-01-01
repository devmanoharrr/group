package com.citizenscience.rewards.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO used by the rewards service when deserialising observations from the data service.
 *
 * Matches the JSON returned by the Crowdsourced Data microservice and exposes
 * helper accessors for reward calculations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaterObservationRecord {

    /** Unique identifier of the observation. */
    private String id;
    /** Citizen identifier that submitted the record. */
    private String citizenId;
    /** Postcode describing the observation location. */
    private String postcode;
    /** Temperature measurement in degrees Celsius. */
    private Double temperature;
    /** pH reading provided with the observation. */
    private Double ph;
    /** Alkalinity measurement in mg/L. */
    private Double alkalinity;
    /** Turbidity measurement in NTU. */
    private Double turbidity;
    /** Flag indicating whether the submission was complete. */
    private boolean complete;
    /** Timestamp of when the observation was submitted. */
    private LocalDateTime submissionTimestamp;
    /** Qualitative observations associated with the record. */
    private Set<String> observations;
    /** Base64 encoded image payloads. */
    private List<String> images;

    /**
     * @return unique identifier of the observation
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the observation identifier.
     *
     * @param id observation UUID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return identifier of the citizen who submitted the record
     */
    public String getCitizenId() {
        return citizenId;
    }

    /**
     * Sets the citizen identifier associated with the observation.
     *
     * @param citizenId sequential citizen identifier
     */
    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    /**
     * @return postcode describing the observation location
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postcode describing where the observation was taken.
     *
     * @param postcode location postcode
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * @return temperature measurement or {@code null} when omitted
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Sets the optional temperature measurement.
     *
     * @param temperature degrees Celsius value
     */
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /**
     * @return pH measurement or {@code null} when omitted
     */
    public Double getPh() {
        return ph;
    }

    /**
     * Sets the optional pH measurement.
     *
     * @param ph acidity value
     */
    public void setPh(Double ph) {
        this.ph = ph;
    }

    /**
     * @return alkalinity measurement or {@code null} when omitted
     */
    public Double getAlkalinity() {
        return alkalinity;
    }

    /**
     * Sets the optional alkalinity measurement.
     *
     * @param alkalinity concentration in mg/L
     */
    public void setAlkalinity(Double alkalinity) {
        this.alkalinity = alkalinity;
    }

    /**
     * @return turbidity measurement or {@code null} when omitted
     */
    public Double getTurbidity() {
        return turbidity;
    }

    /**
     * Sets the optional turbidity measurement.
     *
     * @param turbidity NTU value
     */
    public void setTurbidity(Double turbidity) {
        this.turbidity = turbidity;
    }

    /**
     * @return {@code true} when the observation is marked as complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Marks whether the observation satisfied completeness rules.
     *
     * @param complete completeness flag from the data service
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * @return timestamp describing when the observation was submitted
     */
    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    /**
     * Sets the submission timestamp.
     *
     * @param submissionTimestamp recorded submission time
     */
    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    /**
     * @return qualitative observations supplied with the record
     */
    public Set<String> getObservations() {
        return observations;
    }

    /**
     * Sets the qualitative observations.
     *
     * @param observations descriptive labels returned by the data service
     */
    public void setObservations(Set<String> observations) {
        this.observations = observations;
    }

    /**
     * @return Base64 encoded images stored with the observation
     */
    public List<String> getImages() {
        return images;
    }

    /**
     * Sets the Base64 encoded images associated with the observation.
     *
     * @param images list of Base64 strings
     */
    public void setImages(List<String> images) {
        this.images = images;
    }
}
