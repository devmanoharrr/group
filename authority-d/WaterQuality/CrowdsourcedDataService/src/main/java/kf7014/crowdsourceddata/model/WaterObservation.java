package kf7014.crowdsourceddata.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "observations")
public class WaterObservation {

    @Id
    private String id;
    @NotBlank
    private String citizenId;
    @NotBlank
    private String postcode;
    private MeasurementSet measurements;
    private List<String> observations; // Clear, Cloudy, Murky, Foamy, Oily, Discoloured, Presence of Odour
    private List<String> imageData; // base64 strings or URLs (max 3 by validation)
    private Instant submittedAt;

    public WaterObservation() {
        this.id = UUID.randomUUID().toString();
        this.submittedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public MeasurementSet getMeasurements() {
        return measurements;
    }

    public void setMeasurements(MeasurementSet measurements) {
        this.measurements = measurements;
    }

    public List<String> getObservations() {
        return observations;
    }

    public void setObservations(List<String> observations) {
        this.observations = observations;
    }

    public List<String> getImageData() {
        return imageData;
    }

    public void setImageData(List<String> imageData) {
        this.imageData = imageData;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}


