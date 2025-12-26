package com.waterquality.rewards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.waterquality.rewards.client.DataServiceClient;
import com.waterquality.rewards.model.dto.RewardsDTOs;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RewardsService.
 * 
 * Tests the business logic for:
 * - Processing observations and awarding points
 * - Badge assignment based on points
 * - Leaderboard generation
 * - Citizen reward retrieval
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Rewards Service Tests")
class RewardsServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private RewardsService rewardsService;

    @BeforeEach
    void setUp() {
        // Clear any existing data before each test
        rewardsService.clearAllData();
    }

    @Test
    @DisplayName("Should process observation and award base points (10)")
    void testProcessObservation_BasePoints() {
        // Arrange
        RewardsDTOs.ObservationDTO observation = RewardsDTOs.ObservationDTO.builder()
                .id("obs-1")
                .citizenId("user001")
                .postcode("NE1 8ST")
                .temperature(18.5)
                .ph(7.2)
                .processed(false)
                .build();

        when(dataServiceClient.getUnprocessedObservations())
                .thenReturn(List.of(observation));
        doNothing().when(dataServiceClient).markAsProcessed(anyString());

        // Act
        RewardsDTOs.ProcessResponse response = rewardsService.processObservations();

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getProcessedCount());
        
        // Check citizen points
        RewardsDTOs.CitizenRewardResponse citizenRewards = 
                rewardsService.getCitizenRewards("user001");
        assertEquals(10, citizenRewards.getPoints());
        
        verify(dataServiceClient, times(1)).markAsProcessed("obs-1");
    }

    @Test
    @DisplayName("Should award bonus points for complete record (20 total)")
    void testProcessObservation_CompleteRecord() {
        // Arrange
        RewardsDTOs.ObservationDTO completeObservation = RewardsDTOs.ObservationDTO.builder()
                .id("obs-2")
                .citizenId("user002")
                .postcode("NE1 8ST")
                .temperature(18.5)
                .ph(7.2)
                .alkalinity(120.0)
                .turbidity(2.5)
                .observations(Arrays.asList("Clear", "Presence of Odour"))
                .processed(false)
                .build();

        when(dataServiceClient.getUnprocessedObservations())
                .thenReturn(List.of(completeObservation));
        doNothing().when(dataServiceClient).markAsProcessed(anyString());

        // Act
        RewardsDTOs.ProcessResponse response = rewardsService.processObservations();

        // Assert
        assertEquals(1, response.getProcessedCount());
        
        RewardsDTOs.CitizenRewardResponse citizenRewards = 
                rewardsService.getCitizenRewards("user002");
        assertEquals(20, citizenRewards.getPoints()); // 10 + 10 bonus
    }

    @Test
    @DisplayName("Should award Bronze badge at 100 points")
    void testBadgeAward_Bronze() {
        // Arrange - Submit 10 observations to reach 100 points
        List<RewardsDTOs.ObservationDTO> observations = createMultipleObservations("user003", 10);
        
        when(dataServiceClient.getUnprocessedObservations()).thenReturn(observations);
        doNothing().when(dataServiceClient).markAsProcessed(anyString());

        // Act
        rewardsService.processObservations();

        // Assert
        RewardsDTOs.CitizenRewardResponse rewards = rewardsService.getCitizenRewards("user003");
        assertEquals(100, rewards.getPoints());
        assertTrue(rewards.getBadges().contains("Bronze"));
        assertEquals("Bronze", rewards.getTier());
    }

    @Test
    @DisplayName("Should award Silver badge at 200 points")
    void testBadgeAward_Silver() {
        // Arrange - Submit 20 observations to reach 200 points
        List<RewardsDTOs.ObservationDTO> observations = createMultipleObservations("user004", 20);
        
        when(dataServiceClient.getUnprocessedObservations()).thenReturn(observations);
        doNothing().when(dataServiceClient).markAsProcessed(anyString());

        // Act
        rewardsService.processObservations();

        // Assert
        RewardsDTOs.CitizenRewardResponse rewards = rewardsService.getCitizenRewards("user004");
        assertEquals(200, rewards.getPoints());
        assertTrue(rewards.getBadges().contains("Silver"));
        assertTrue(rewards.getBadges().contains("Bronze"));
        assertEquals("Silver", rewards.getTier());
    }

    @Test
    @DisplayName("Should award Gold badge at 500 points")
    void testBadgeAward_Gold() {
        // Arrange - Submit 50 observations to reach 500 points
        List<RewardsDTOs.ObservationDTO> observations = createMultipleObservations("user005", 50);
        
        when(dataServiceClient.getUnprocessedObservations()).thenReturn(observations);
        doNothing().when(dataServiceClient).markAsProcessed(anyString());

        // Act
        rewardsService.processObservations();

        // Assert
        RewardsDTOs.CitizenRewardResponse rewards = rewardsService.getCitizenRewards("user005");
        assertEquals(500, rewards.getPoints());
        assertTrue(rewards.getBadges().contains("Gold"));
        assertTrue(rewards.getBadges().contains("Silver"));
        assertTrue(rewards.getBadges().contains("Bronze"));
        assertEquals("Gold", rewards.getTier());
    }

    @Test
    @DisplayName("Should generate leaderboard with correct ranking")
    void testLeaderboard() {
        // Arrange - Create observations for multiple citizens with different points
        RewardsDTOs.ObservationDTO obs1 = createObservation("obs-1", "user001", false); // 10 pts
        RewardsDTOs.ObservationDTO obs2 = createObservation("obs-2", "user002", true);  // 20 pts
        RewardsDTOs.ObservationDTO obs3 = createObservation("obs-3", "user002", false); // +10 = 30 total
        
        when(dataServiceClient.getUnprocessedObservations())
                .thenReturn(Arrays.asList(obs1, obs2, obs3));
        doNothing().when(dataServiceClient).markAsProcessed(anyString());

        // Act
        rewardsService.processObservations();
        RewardsDTOs.LeaderboardResponse leaderboard = rewardsService.getLeaderboard(10);

        // Assert
        assertNotNull(leaderboard);
        assertEquals(2, leaderboard.getTotalCitizens());
        assertEquals(2, leaderboard.getLeaderboard().size());
        
        // Check ranking (user002 should be first with 30 points)
        RewardsDTOs.LeaderboardEntry first = leaderboard.getLeaderboard().get(0);
        assertEquals("user002", first.getCitizenId());
        assertEquals(30, first.getPoints());
        assertEquals(1, first.getRank());
        
        RewardsDTOs.LeaderboardEntry second = leaderboard.getLeaderboard().get(1);
        assertEquals("user001", second.getCitizenId());
        assertEquals(10, second.getPoints());
        assertEquals(2, second.getRank());
    }

    @Test
    @DisplayName("Should handle empty unprocessed observations")
    void testProcessObservations_NoData() {
        // Arrange
        when(dataServiceClient.getUnprocessedObservations()).thenReturn(List.of());

        // Act
        RewardsDTOs.ProcessResponse response = rewardsService.processObservations();

        // Assert
        assertEquals(0, response.getProcessedCount());
        assertTrue(response.getMessage().contains("No unprocessed"));
        verify(dataServiceClient, never()).markAsProcessed(anyString());
    }

    @Test
    @DisplayName("Should return zero points for new citizen")
    void testGetCitizenRewards_NewCitizen() {
        // Act
        RewardsDTOs.CitizenRewardResponse rewards = rewardsService.getCitizenRewards("newUser");

        // Assert
        assertEquals("newUser", rewards.getCitizenId());
        assertEquals(0, rewards.getPoints());
        assertTrue(rewards.getBadges().isEmpty());
        assertEquals("Beginner", rewards.getTier());
    }

    // Helper methods

    private RewardsDTOs.ObservationDTO createObservation(String id, String citizenId, boolean complete) {
        RewardsDTOs.ObservationDTO.ObservationDTOBuilder builder = RewardsDTOs.ObservationDTO.builder()
                .id(id)
                .citizenId(citizenId)
                .postcode("NE1 8ST")
                .processed(false);

        if (complete) {
            builder.temperature(18.5)
                   .ph(7.2)
                   .alkalinity(120.0)
                   .turbidity(2.5)
                   .observations(Arrays.asList("Clear"));
        } else {
            builder.temperature(18.5);
        }

        return builder.build();
    }

    private List<RewardsDTOs.ObservationDTO> createMultipleObservations(String citizenId, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> RewardsDTOs.ObservationDTO.builder()
                        .id("obs-" + i)
                        .citizenId(citizenId)
                        .postcode("NE1 8ST")
                        .temperature(18.5)
                        .processed(false)
                        .build())
                .toList();
    }
}
