package kf7014.rewards.service;

import org.springframework.stereotype.Service;
import kf7014.rewards.client.CrowdsourcedClient;
import kf7014.rewards.model.RewardSummary;
import kf7014.rewards.config.BadgeProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RewardComputationService {

    private final CrowdsourcedClient crowdsourcedClient;
    private final BadgeProperties badgeProperties;
    private final Map<String, RewardSummary> cacheByCitizen = new HashMap<>();

    public RewardComputationService(CrowdsourcedClient crowdsourcedClient, BadgeProperties badgeProperties) {
        this.crowdsourcedClient = crowdsourcedClient;
        this.badgeProperties = badgeProperties;
    }

    public RewardSummary recomputeForCitizen(String citizenId) {
        List<CrowdsourcedClient.ObservationDto> list = crowdsourcedClient.findByCitizenId(citizenId);
        int points = 0;
        for (CrowdsourcedClient.ObservationDto obs : list) {
            if (!isValid(obs)) continue; // defensive; CDS already validates
            points += 10; // valid submission
            if (isComplete(obs)) points += 10; // bonus for complete
        }
        String badge = computeBadge(points);
        RewardSummary summary = new RewardSummary(citizenId, points, badge);
        cacheByCitizen.put(citizenId, summary);
        return summary;
    }

    public RewardSummary getSummary(String citizenId) {
        return cacheByCitizen.getOrDefault(citizenId, new RewardSummary(citizenId, 0, ""));
    }

    private boolean isValid(CrowdsourcedClient.ObservationDto obs) {
        boolean hasPostcode = obs.postcode != null && !obs.postcode.isBlank();
        boolean hasMeasurement = obs.measurements != null && (
                obs.measurements.temperatureCelsius != null ||
                obs.measurements.ph != null ||
                obs.measurements.alkalinityMgPerL != null ||
                obs.measurements.turbidityNtu != null
        );
        boolean hasObservation = obs.observations != null && !obs.observations.isEmpty();
        return hasPostcode && (hasMeasurement || hasObservation);
    }

    private boolean isComplete(CrowdsourcedClient.ObservationDto obs) {
        boolean hasAllMeasurements = obs.measurements != null &&
                obs.measurements.temperatureCelsius != null &&
                obs.measurements.ph != null &&
                obs.measurements.alkalinityMgPerL != null &&
                obs.measurements.turbidityNtu != null;
        boolean hasAtLeastOneObservation = obs.observations != null && !obs.observations.isEmpty();
        // Images optional; completeness defined as all fields present: postcode, all measurements, at least one observation
        return obs.citizenId != null && !obs.citizenId.isBlank() &&
                obs.postcode != null && !obs.postcode.isBlank() &&
                hasAllMeasurements && hasAtLeastOneObservation;
    }

    private String computeBadge(int points) {
        for (BadgeProperties.BadgeRule rule : badgeProperties.getBadgesSortedDesc()) {
            if (points >= rule.getThreshold()) {
                return rule.getName();
            }
        }
        return "";
    }
}


