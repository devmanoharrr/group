package kf7014.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import kf7014.rewards.model.RewardSummary;
import kf7014.rewards.service.RewardComputationService;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardsController.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RewardComputationService service;

    @Test
    void recompute_withValidCitizenId_returnsOk() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001", 20, "Bronze");
        Mockito.when(service.recomputeForCitizen("citizen-001")).thenReturn(summary);
        
        mockMvc.perform(post("/api/rewards/recompute/citizen-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenId", is("citizen-001")))
                .andExpect(jsonPath("$.totalPoints", is(20)))
                .andExpect(jsonPath("$.badge", is("Bronze")));
    }

    @Test
    void recompute_withEmptyCitizenId_returnsNotFound() throws Exception {
        // Empty path variable results in 404 as Spring can't match the route
        // This is expected behavior - empty citizenId is not a valid REST endpoint
        mockMvc.perform(post("/api/rewards/recompute/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void recompute_withSpecialCharactersInCitizenId_returnsOk() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001-test", 10, "");
        Mockito.when(service.recomputeForCitizen("citizen-001-test")).thenReturn(summary);
        
        mockMvc.perform(post("/api/rewards/recompute/citizen-001-test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void recompute_withLongCitizenId_returnsOk() throws Exception {
        String longId = "a".repeat(100);
        RewardSummary summary = new RewardSummary(longId, 0, "");
        Mockito.when(service.recomputeForCitizen(longId)).thenReturn(summary);
        
        mockMvc.perform(post("/api/rewards/recompute/" + longId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void get_withValidCitizenId_returnsOk() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001", 20, "Bronze");
        Mockito.when(service.getSummary("citizen-001")).thenReturn(summary);
        
        mockMvc.perform(get("/api/rewards/citizen-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenId", is("citizen-001")))
                .andExpect(jsonPath("$.totalPoints", is(20)))
                .andExpect(jsonPath("$.badge", is("Bronze")));
    }

    @Test
    void get_withNonExistentCitizenId_returnsDefault() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-999", 0, "");
        Mockito.when(service.getSummary("citizen-999")).thenReturn(summary);
        
        mockMvc.perform(get("/api/rewards/citizen-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenId", is("citizen-999")))
                .andExpect(jsonPath("$.totalPoints", is(0)))
                .andExpect(jsonPath("$.badge", is("")));
    }

    @Test
    void get_withEmptyCitizenId_returnsNotFound() throws Exception {
        // Empty path variable results in 404 as Spring can't match the route
        // This is expected behavior - empty citizenId is not a valid REST endpoint
        mockMvc.perform(get("/api/rewards/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_withZeroPoints_returnsOk() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001", 0, "");
        Mockito.when(service.getSummary("citizen-001")).thenReturn(summary);
        
        mockMvc.perform(get("/api/rewards/citizen-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(0)));
    }

    @Test
    void get_withHighPoints_returnsOk() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001", 1000, "Gold");
        Mockito.when(service.getSummary("citizen-001")).thenReturn(summary);
        
        mockMvc.perform(get("/api/rewards/citizen-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(1000)))
                .andExpect(jsonPath("$.badge", is("Gold")));
    }

    @Test
    void get_withEmptyBadge_returnsOk() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001", 50, "");
        Mockito.when(service.getSummary("citizen-001")).thenReturn(summary);
        
        mockMvc.perform(get("/api/rewards/citizen-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badge", is("")));
    }

    @Test
    void recompute_verifiesServiceCalled() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001", 20, "");
        Mockito.when(service.recomputeForCitizen("citizen-001")).thenReturn(summary);
        
        mockMvc.perform(post("/api/rewards/recompute/citizen-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        Mockito.verify(service, Mockito.times(1)).recomputeForCitizen("citizen-001");
    }

    @Test
    void get_verifiesServiceCalled() throws Exception {
        RewardSummary summary = new RewardSummary("citizen-001", 20, "");
        Mockito.when(service.getSummary("citizen-001")).thenReturn(summary);
        
        mockMvc.perform(get("/api/rewards/citizen-001"))
                .andExpect(status().isOk());
        
        Mockito.verify(service, Mockito.times(1)).getSummary("citizen-001");
    }
}


