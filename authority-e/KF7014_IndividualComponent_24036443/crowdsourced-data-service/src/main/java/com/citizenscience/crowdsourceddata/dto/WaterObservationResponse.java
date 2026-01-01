package com.citizenscience.crowdsourceddata.dto;

import com.citizenscience.crowdsourceddata.model.ObservationCondition;
import com.citizenscience.crowdsourceddata.model.WaterObservation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Response DTO published when an observation has been stored.
 *
 * Presents all captured readings, qualitative observations, Base64 images, and
 * a derived completeness flag so callers can understand reward implications.
 */
public class WaterObservationResponse {

    /** Unique identifier for the observation. */
    private String id;
    /** Citizen identifier that submitted the observation. */
    private String citizenId;
    /** Postcode describing the sampling location. */
    private String postcode;
    /** Recorded temperature in degrees Celsius. */
    private Double temperature;
    /** Recorded pH level. */
    private Double ph;
    /** Recorded alkalinity measurement. */
    private Double alkalinity;
    /** Recorded turbidity measurement. */
    private Double turbidity;
    /** Timestamp describing when the submission was saved. */
    private LocalDateTime submissionTimestamp;
    /** Qualitative observations converted into enum values. */
    private Set<ObservationCondition> observations;
    /** Stored Base64 images associated with the submission. */
    private List<String> images;

    /**
     * Creates a response populated with all observation details.
     *
     * @param id                  observation identifier
     * @param citizenId           citizen identifier
     * @param postcode            postcode supplied during submission
     * @param temperature         recorded temperature
     * @param ph                  recorded pH value
     * @param alkalinity          recorded alkalinity value
     * @param turbidity           recorded turbidity value
     * @param submissionTimestamp timestamp of persistence
     * @param observations        qualitative observation set
     * @param images              list of Base64 encoded image payloads
     */
    public WaterObservationResponse(String id, String citizenId, String postcode, Double temperature, Double ph,
                                     Double alkalinity, Double turbidity,
                                     LocalDateTime submissionTimestamp, Set<ObservationCondition> observations,
                                     List<String> images) {
        this.id = id;
        this.citizenId = citizenId;
        this.postcode = postcode;
        this.temperature = temperature;
        this.ph = ph;
        this.alkalinity = alkalinity;
        this.turbidity = turbidity;
        this.submissionTimestamp = submissionTimestamp;
        this.observations = observations;
        this.images = images;
    }

    /**
     * Maps a {@link WaterObservation} entity to an API response.
     *
     * @param observation entity loaded from the database
     * @return response DTO containing serialisable data
     */
    public static WaterObservationResponse fromEntity(WaterObservation observation) {
        List<String> encodedImages = observation.getImages() == null ? List.of()
                : List.copyOf(observation.getImages());

        return new WaterObservationResponse(
                observation.getId(),
                observation.getCitizenId(),
                observation.getPostcode(),
                observation.getTemperature(),
                observation.getPh(),
                observation.getAlkalinity(),
                observation.getTurbidity(),
                observation.getSubmissionTimestamp(),
                observation.getConditions(),
                encodedImages
        );
    }

    /**
     * @return unique identifier for the observation
     */
    public String getId() {
        return id;
    }

    /**
     * @return identifier of the citizen that submitted the record
     */
    public String getCitizenId() {
        return citizenId;
    }

    /**
     * @return postcode recorded for the observation
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * @return recorded temperature value
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * @return recorded pH value
     */
    public Double getPh() {
        return ph;
    }

    /**
     * @return recorded alkalinity value
     */
    public Double getAlkalinity() {
        return alkalinity;
    }

    /**
     * @return recorded turbidity value
     */
    public Double getTurbidity() {
        return turbidity;
    }

    /**
     * Provides a nested measurement view for clients expecting a grouped object.
     *
     * @return nested measurement view or {@code null} when no measurements exist
     */
    @JsonProperty("measurements")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MeasurementsView getMeasurements() {
        if (temperature == null && ph == null && alkalinity == null && turbidity == null) {
            return null;
        }
        return new MeasurementsView(temperature, ph, alkalinity, turbidity);
    }

    /**
     * Indicates whether the observation meets all completeness criteria.
     *
     * @return {@code true} when the observation contains all measurements,
     * observations, images, and a postcode
     */
    public boolean isComplete() {
        boolean hasAllMeasurements = temperature != null && ph != null && alkalinity != null && turbidity != null;
        boolean hasObservations = observations != null && !observations.isEmpty();
        boolean hasImages = images != null && !images.isEmpty();
        boolean hasPostcode = postcode != null && !postcode.isBlank();
        return hasPostcode && hasAllMeasurements && hasObservations && hasImages;
    }

    /**
     * @return timestamp describing when the observation was submitted
     */
    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    /**
     * @return qualitative observations linked to the record
     */
    public Set<ObservationCondition> getObservations() {
        return observations;
    }

    /**
     * @return stored Base64 image payloads
     */
    public List<String> getImages() {
        return images;
    }

    /**
     * Nested projection that groups measurement fields for JSON serialisation.
     */
    public static class MeasurementsView {
        /** Temperature measurement exposed in the grouped view. */
        private final Double temperature;
        /** pH measurement exposed in the grouped view. */
        private final Double ph;
        /** Alkalinity measurement exposed in the grouped view. */
        private final Double alkalinity;
        /** Turbidity measurement exposed in the grouped view. */
        private final Double turbidity;

        /**
         * Builds an immutable projection of measurement values.
         *
         * @param temperature temperature value
         * @param ph          pH value
         * @param alkalinity  alkalinity value
         * @param turbidity   turbidity value
         */
        public MeasurementsView(Double temperature, Double ph, Double alkalinity, Double turbidity) {
            this.temperature = temperature;
            this.ph = ph;
            this.alkalinity = alkalinity;
            this.turbidity = turbidity;
        }

        /**
         * @return temperature value for the projection
         */
        public Double getTemperature() {
            return temperature;
        }

        /**
         * @return pH value for the projection
         */
        public Double getPh() {
            return ph;
        }

        /**
         * @return alkalinity value for the projection
         */
        public Double getAlkalinity() {
            return alkalinity;
        }

        /**
         * @return turbidity value for the projection
         */
        public Double getTurbidity() {
            return turbidity;
        }
    }
}
