package kf7014.rewards.service;

import kf7014.rewards.client.CrowdsourcedClient;
import kf7014.rewards.config.BadgeProperties;
import kf7014.rewards.model.RewardSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardComputationServiceTest {

    @Mock
    private CrowdsourcedClient crowdsourcedClient;

    @Mock
    private BadgeProperties badgeProperties;

    private RewardComputationService service;

    @BeforeEach
    void setUp() {
        service = new RewardComputationService(crowdsourcedClient, badgeProperties);
    }

    @Test
    void recomputeForCitizen_withNoObservations_returnsZeroPoints() {
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(Collections.emptyList());
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals("citizen-001", result.getCitizenId());
        assertEquals(0, result.getTotalPoints());
        assertEquals("", result.getBadge());
        verify(crowdsourcedClient).findByCitizenId("citizen-001");
    }

    @Test
    void recomputeForCitizen_withValidObservation_returnsTenPoints() {
        CrowdsourcedClient.ObservationDto obs = createValidObservation("citizen-001", "NE1 1AA");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints());
        verify(crowdsourcedClient).findByCitizenId("citizen-001");
    }

    @Test
    void recomputeForCitizen_withCompleteObservation_returnsTwentyPoints() {
        CrowdsourcedClient.ObservationDto obs = createCompleteObservation("citizen-001", "NE1 1AA");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(20, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withMultipleObservations_sumsPointsCorrectly() {
        CrowdsourcedClient.ObservationDto valid1 = createValidObservation("citizen-001", "NE1 1AA");
        CrowdsourcedClient.ObservationDto valid2 = createValidObservation("citizen-001", "NE2 2BB");
        CrowdsourcedClient.ObservationDto complete = createCompleteObservation("citizen-001", "NE3 3CC");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(valid1, valid2, complete));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(40, result.getTotalPoints()); // 10 + 10 + 20
    }

    @Test
    void recomputeForCitizen_withInvalidObservation_skipsInvalid() {
        CrowdsourcedClient.ObservationDto invalid = new CrowdsourcedClient.ObservationDto();
        invalid.postcode = null; // Missing postcode
        invalid.citizenId = "citizen-001";
        
        CrowdsourcedClient.ObservationDto valid = createValidObservation("citizen-001", "NE1 1AA");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(invalid, valid));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints()); // Only valid observation counted
    }

    @Test
    void recomputeForCitizen_withObservationMissingPostcode_skipsIt() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = null; // Missing postcode
        obs.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        obs.measurements.ph = 7.0;
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(0, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withBlankPostcode_skipsIt() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "   "; // Blank postcode
        obs.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        obs.measurements.ph = 7.0;
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(0, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withObservationOnlyMeasurements_valid() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        obs.measurements.ph = 7.0;
        obs.observations = null; // No observations, only measurements
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withObservationOnlyObservations_valid() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = null; // No measurements
        obs.observations = List.of("Clear");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withObservationMissingBothMeasurementsAndObservations_skipsIt() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = null;
        obs.observations = null;
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(0, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withEmptyObservationsList_skipsIt() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = null;
        obs.observations = Collections.emptyList();
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(0, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withCompleteObservationMissingCitizenId_notComplete() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = null; // Missing citizenId
        obs.postcode = "NE1 1AA";
        obs.measurements = createFullMeasurements();
        obs.observations = List.of("Clear");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints()); // Valid but not complete
    }

    @Test
    void recomputeForCitizen_withCompleteObservationMissingOneMeasurement_notComplete() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        obs.measurements.temperatureCelsius = 20.0;
        obs.measurements.ph = 7.0;
        obs.measurements.alkalinityMgPerL = 100.0;
        obs.measurements.turbidityNtu = null; // Missing one measurement
        obs.observations = List.of("Clear");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints()); // Valid but not complete
    }

    @Test
    void recomputeForCitizen_withCompleteObservationMissingObservations_notComplete() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = createFullMeasurements();
        obs.observations = null; // Missing observations
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints()); // Valid but not complete
    }

    @Test
    void recomputeForCitizen_withCompleteObservationEmptyObservations_notComplete() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = createFullMeasurements();
        obs.observations = Collections.emptyList(); // Empty observations
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints()); // Valid but not complete
    }

    @Test
    void recomputeForCitizen_withBadgeRules_assignsCorrectBadge() {
        BadgeProperties.BadgeRule bronze = new BadgeProperties.BadgeRule();
        bronze.setName("Bronze");
        bronze.setThreshold(100);
        
        BadgeProperties.BadgeRule silver = new BadgeProperties.BadgeRule();
        silver.setName("Silver");
        silver.setThreshold(200);
        
        BadgeProperties.BadgeRule gold = new BadgeProperties.BadgeRule();
        gold.setName("Gold");
        gold.setThreshold(500);

        // Create 6 complete observations = 120 points (Bronze)
        List<CrowdsourcedClient.ObservationDto> observations = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            observations.add(createCompleteObservation("citizen-001", "NE1 1AA"));
        }
        
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(observations);
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(List.of(gold, silver, bronze));

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(120, result.getTotalPoints());
        assertEquals("Bronze", result.getBadge());
    }

    @Test
    void recomputeForCitizen_withBadgeRules_exactThreshold_assignsBadge() {
        BadgeProperties.BadgeRule bronze = new BadgeProperties.BadgeRule();
        bronze.setName("Bronze");
        bronze.setThreshold(100);
        
        // Create exactly 5 complete observations = 100 points (exact threshold)
        List<CrowdsourcedClient.ObservationDto> observations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            observations.add(createCompleteObservation("citizen-001", "NE1 1AA"));
        }
        
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(observations);
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(List.of(bronze));

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(100, result.getTotalPoints());
        assertEquals("Bronze", result.getBadge());
    }

    @Test
    void recomputeForCitizen_withBadgeRules_belowThreshold_noBadge() {
        BadgeProperties.BadgeRule bronze = new BadgeProperties.BadgeRule();
        bronze.setName("Bronze");
        bronze.setThreshold(100);
        
        // Create 4 complete observations = 80 points (below threshold)
        List<CrowdsourcedClient.ObservationDto> observations = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            observations.add(createCompleteObservation("citizen-001", "NE1 1AA"));
        }
        
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(observations);
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(List.of(bronze));

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(80, result.getTotalPoints());
        assertEquals("", result.getBadge());
    }

    @Test
    void recomputeForCitizen_withBadgeRules_highestBadgeAssigned() {
        BadgeProperties.BadgeRule bronze = new BadgeProperties.BadgeRule();
        bronze.setName("Bronze");
        bronze.setThreshold(100);
        
        BadgeProperties.BadgeRule silver = new BadgeProperties.BadgeRule();
        silver.setName("Silver");
        silver.setThreshold(200);
        
        // Create 15 complete observations = 300 points (should get Silver)
        List<CrowdsourcedClient.ObservationDto> observations = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            observations.add(createCompleteObservation("citizen-001", "NE1 1AA"));
        }
        
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(observations);
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(List.of(silver, bronze));

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(300, result.getTotalPoints());
        assertEquals("Silver", result.getBadge());
    }

    @Test
    void recomputeForCitizen_cachesResult() {
        CrowdsourcedClient.ObservationDto obs = createValidObservation("citizen-001", "NE1 1AA");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result1 = service.recomputeForCitizen("citizen-001");
        RewardSummary result2 = service.getSummary("citizen-001");

        assertEquals(result1.getTotalPoints(), result2.getTotalPoints());
        verify(crowdsourcedClient, times(1)).findByCitizenId("citizen-001");
    }

    @Test
    void getSummary_withCachedResult_returnsCached() {
        CrowdsourcedClient.ObservationDto obs = createValidObservation("citizen-001", "NE1 1AA");
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        service.recomputeForCitizen("citizen-001");
        RewardSummary result = service.getSummary("citizen-001");

        assertEquals(10, result.getTotalPoints());
        verify(crowdsourcedClient, times(1)).findByCitizenId("citizen-001");
    }

    @Test
    void getSummary_withNoCache_returnsDefault() {
        RewardSummary result = service.getSummary("citizen-999");

        assertEquals("citizen-999", result.getCitizenId());
        assertEquals(0, result.getTotalPoints());
        assertEquals("", result.getBadge());
        verify(crowdsourcedClient, never()).findByCitizenId(anyString());
    }

    @Test
    void recomputeForCitizen_withNullCitizenId_handlesGracefully() {
        when(crowdsourcedClient.findByCitizenId(null)).thenReturn(Collections.emptyList());
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen(null);

        assertNotNull(result);
        assertEquals(0, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withEmptyCitizenId_handlesGracefully() {
        when(crowdsourcedClient.findByCitizenId("")).thenReturn(Collections.emptyList());
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("");

        assertNotNull(result);
        assertEquals(0, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withNullResponseFromClient_throwsNullPointerException() {
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(null);
        // Note: badgeProperties.getBadgesSortedDesc() is not called because
        // the code throws NullPointerException when iterating over null list

        assertThrows(NullPointerException.class, () -> {
            service.recomputeForCitizen("citizen-001");
        });
    }

    @Test
    void recomputeForCitizen_withMeasurementPartialData_valid() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        obs.measurements.temperatureCelsius = 20.0; // Only one measurement
        obs.observations = null;
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints());
    }

    @Test
    void recomputeForCitizen_withAllMeasurementTypes_valid() {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = "citizen-001";
        obs.postcode = "NE1 1AA";
        obs.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        obs.measurements.temperatureCelsius = 20.0;
        obs.measurements.ph = 7.0;
        obs.measurements.alkalinityMgPerL = 100.0;
        obs.measurements.turbidityNtu = 5.0;
        obs.observations = null;
        when(crowdsourcedClient.findByCitizenId("citizen-001")).thenReturn(List.of(obs));
        when(badgeProperties.getBadgesSortedDesc()).thenReturn(Collections.emptyList());

        RewardSummary result = service.recomputeForCitizen("citizen-001");

        assertEquals(10, result.getTotalPoints());
    }

    // Helper methods
    private CrowdsourcedClient.ObservationDto createValidObservation(String citizenId, String postcode) {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = citizenId;
        obs.postcode = postcode;
        obs.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        obs.measurements.ph = 7.0;
        obs.observations = List.of("Clear");
        return obs;
    }

    private CrowdsourcedClient.ObservationDto createCompleteObservation(String citizenId, String postcode) {
        CrowdsourcedClient.ObservationDto obs = new CrowdsourcedClient.ObservationDto();
        obs.citizenId = citizenId;
        obs.postcode = postcode;
        obs.measurements = createFullMeasurements();
        obs.observations = List.of("Clear");
        return obs;
    }

    private CrowdsourcedClient.ObservationDto.Measurements createFullMeasurements() {
        CrowdsourcedClient.ObservationDto.Measurements m = new CrowdsourcedClient.ObservationDto.Measurements();
        m.temperatureCelsius = 20.0;
        m.ph = 7.0;
        m.alkalinityMgPerL = 100.0;
        m.turbidityNtu = 5.0;
        return m;
    }
}

