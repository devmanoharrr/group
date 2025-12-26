package citizen.crowdsourced.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CrowdsourcedRequest {

    @NotBlank(message = "citizenId is required")
    private String citizenId;

    @NotBlank(message = "postcode is required")
    private String postcode;

    private Double temperature;
    private Double ph;
    private Double alkalinity;
    private Double turbidity;

    @Size(min = 0, max = 7)
    private List<String> observations;

    // getters/setters
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }
    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }
    public Double getAlkalinity() { return alkalinity; }
    public void setAlkalinity(Double alkalinity) { this.alkalinity = alkalinity; }
    public Double getTurbidity() { return turbidity; }
    public void setTurbidity(Double turbidity) { this.turbidity = turbidity; }
    public List<String> getObservations() { return observations; }
    public void setObservations(List<String> observations) { this.observations = observations; }
}