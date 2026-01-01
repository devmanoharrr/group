package com.citizenscience.rewards.service;

import com.citizenscience.rewards.client.CrowdsourcedDataClient;
import com.citizenscience.rewards.client.WaterObservationRecord;
import com.citizenscience.rewards.model.BadgeLevel;
import com.citizenscience.rewards.model.RewardSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardComputationServiceTest {

    @Mock
    private CrowdsourcedDataClient dataClient;

    @InjectMocks
    private RewardComputationService service;

    @Test
    void calculatesBaseAndBonusPoints() {
        WaterObservationRecord completeRecord = new WaterObservationRecord();
        completeRecord.setComplete(true);

        WaterObservationRecord partialRecord = new WaterObservationRecord();
        partialRecord.setComplete(false);

        when(dataClient.fetchObservationsForCitizen("citizen"))
                .thenReturn(List.of(completeRecord, partialRecord, completeRecord));

        RewardSummary summary = service.calculateRewardsForCitizen("citizen");

        assertThat(summary.getTotalSubmissions()).isEqualTo(3);
        assertThat(summary.getBonusSubmissions()).isEqualTo(2);
        assertThat(summary.getTotalPoints()).isEqualTo(50);
        assertThat(summary.getCurrentBadge()).isEqualTo(BadgeLevel.NONE);
    }

    @Test
    void awardsBadgesAtThresholds() {
        List<WaterObservationRecord> records = IntStream.range(0, 10)
                .mapToObj(i -> {
                    WaterObservationRecord record = new WaterObservationRecord();
                    record.setComplete(true);
                    return record;
                })
                .collect(Collectors.toList());

        when(dataClient.fetchObservationsForCitizen("high-scorer")).thenReturn(records);

        RewardSummary summary = service.calculateRewardsForCitizen("high-scorer");

        assertThat(summary.getTotalPoints()).isEqualTo(200);
        assertThat(summary.getCurrentBadge()).isEqualTo(BadgeLevel.SILVER);
        assertThat(summary.getEarnedBadges()).containsExactly("BRONZE", "SILVER");
    }
}
