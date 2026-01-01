package com.citizenscience.crowdsourceddata.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request payload received when a citizen submits a water observation.
 *
 * Mirrors the JSON structure accepted by the controller, including measurements,
 * qualitative observations, and optional images that must be Base64 encoded.
 */
public class WaterObservationRequest {

    /** Postcode identifying the sampling location. */
    @NotBlank(message = "Postcode is required")
    private String postcode;

    /** Inline temperature measurement shortcut when the nested object is omitted. */
    private Double temperature;

    /** Inline pH measurement shortcut when the nested object is omitted. */
    @DecimalMin(value = "0.0", message = "pH cannot be negative")
    @DecimalMax(value = "14.0", message = "pH cannot exceed 14")
    private Double ph;

    /** Inline alkalinity measurement shortcut when the nested object is omitted. */
    @DecimalMin(value = "0.0", message = "Alkalinity cannot be negative")
    private Double alkalinity;

    /** Inline turbidity measurement shortcut when the nested object is omitted. */
    @DecimalMin(value = "0.0", message = "Turbidity cannot be negative")
    private Double turbidity;

    /** Optional nested measurements object used by some clients. */
    @Valid
    private MeasurementsPayload measurements;

    /** Qualitative descriptors such as Clear, Cloudy, or Oily. */
    private List<@NotNull String> observations;

    /** Optional Base64 encoded images supplied by the citizen. */
    private List<String> images;

    /**
     * @return postcode supplied in the submission
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postcode supplied by the citizen.
     *
     * @param postcode trimmed postcode string
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * @return temperature value when provided
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Captures the temperature measurement provided outside the nested object.
     *
     * @param temperature temperature in degrees Celsius
     */
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /**
     * @return pH measurement when supplied directly
     */
    public Double getPh() {
        return ph;
    }

    /**
     * Sets the inline pH measurement.
     *
     * @param ph acidity value between 0 and 14
     */
    public void setPh(Double ph) {
        this.ph = ph;
    }

    /**
     * @return alkalinity reading when supplied directly
     */
    public Double getAlkalinity() {
        return alkalinity;
    }

    /**
     * Sets the inline alkalinity measurement.
     *
     * @param alkalinity concentration in mg/L
     */
    public void setAlkalinity(Double alkalinity) {
        this.alkalinity = alkalinity;
    }

    /**
     * @return turbidity reading when supplied directly
     */
    public Double getTurbidity() {
        return turbidity;
    }

    /**
     * Sets the inline turbidity measurement.
     *
     * @param turbidity NTU value representing clarity
     */
    public void setTurbidity(Double turbidity) {
        this.turbidity = turbidity;
    }

    /**
     * @return nested measurements payload if present
     */
    public MeasurementsPayload getMeasurements() {
        return measurements;
    }

    /**
     * Accepts the nested measurements payload and mirrors its values to the
     * top-level shortcut properties for compatibility.
     *
     * @param measurements wrapper containing measurement values
     */
    public void setMeasurements(MeasurementsPayload measurements) {
        this.measurements = measurements;
        if (measurements != null) {
            this.temperature = measurements.getTemperature();
            this.ph = measurements.getPh();
            this.alkalinity = measurements.getAlkalinity();
            this.turbidity = measurements.getTurbidity();
        }
    }

    /**
     * @return qualitative observations attached to the submission
     */
    public List<String> getObservations() {
        return observations;
    }

    /**
     * Sets the qualitative observations provided by the citizen.
     *
     * @param observations descriptive labels such as "Clear" or "Foamy"
     */
    public void setObservations(List<String> observations) {
        this.observations = observations;
    }

    /**
     * @return Base64 encoded images that accompanied the submission
     */
    public List<String> getImages() {
        return images;
    }

    /**
     * Sets the optional Base64 encoded image payloads.
     *
     * @param images image strings provided in the request
     */
    public void setImages(List<String> images) {
        this.images = images;
    }

    /**
     * Nested DTO allowing clients to send measurements under a single object.
     */
    public static class MeasurementsPayload {

        /** Temperature measurement within the nested structure. */
        private Double temperature;

        /** pH measurement within the nested structure. */
        @DecimalMin(value = "0.0", message = "pH cannot be negative")
        @DecimalMax(value = "14.0", message = "pH cannot exceed 14")
        private Double ph;

        /** Alkalinity measurement within the nested structure. */
        @DecimalMin(value = "0.0", message = "Alkalinity cannot be negative")
        private Double alkalinity;

        /** Turbidity measurement within the nested structure. */
        @DecimalMin(value = "0.0", message = "Turbidity cannot be negative")
        private Double turbidity;

        /**
         * @return nested temperature value
         */
        public Double getTemperature() {
            return temperature;
        }

        /**
         * Sets the nested temperature value.
         *
         * @param temperature temperature in degrees Celsius
         */
        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        /**
         * @return nested pH value
         */
        public Double getPh() {
            return ph;
        }

        /**
         * Sets the nested pH value.
         *
         * @param ph acidity value between 0 and 14
         */
        public void setPh(Double ph) {
            this.ph = ph;
        }

        /**
         * @return nested alkalinity value
         */
        public Double getAlkalinity() {
            return alkalinity;
        }

        /**
         * Sets the nested alkalinity value.
         *
         * @param alkalinity concentration in mg/L
         */
        public void setAlkalinity(Double alkalinity) {
            this.alkalinity = alkalinity;
        }

        /**
         * @return nested turbidity value
         */
        public Double getTurbidity() {
            return turbidity;
        }

        /**
         * Sets the nested turbidity value.
         *
         * @param turbidity NTU value representing clarity
         */
        public void setTurbidity(Double turbidity) {
            this.turbidity = turbidity;
        }
    }
}
