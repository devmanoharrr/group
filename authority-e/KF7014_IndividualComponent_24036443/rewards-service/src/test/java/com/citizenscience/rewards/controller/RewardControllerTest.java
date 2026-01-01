package com.citizenscience.rewards.controller;

import com.citizenscience.rewards.exception.ObservationFetchException;
import com.citizenscience.rewards.model.BadgeLevel;
import com.citizenscience.rewards.model.RewardSummary;
import com.citizenscience.rewards.service.RewardComputationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RewardController.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RewardComputationService computationService;

    @Test
    void getRewardsForCitizenReturnsOk() throws Exception {
        RewardSummary summary = new RewardSummary("citizen", 1, 10, 0, BadgeLevel.NONE, List.of());
        Mockito.when(computationService.calculateRewardsForCitizen("citizen")).thenReturn(summary);

        mockMvc.perform(get("/rewards/citizen").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getRewardsReturnsBadGatewayWhenFetchFails() throws Exception {
        Mockito.when(computationService.calculateRewardsForCitizen("citizen"))
                .thenThrow(new ObservationFetchException("downstream unavailable"));

        mockMvc.perform(get("/rewards/citizen").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("downstream unavailable"));
    }
}
