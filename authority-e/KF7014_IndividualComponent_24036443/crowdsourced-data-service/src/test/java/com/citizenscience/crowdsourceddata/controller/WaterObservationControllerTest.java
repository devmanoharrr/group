package com.citizenscience.crowdsourceddata.controller;

import com.citizenscience.crowdsourceddata.dto.WaterObservationRequest;
import com.citizenscience.crowdsourceddata.dto.WaterObservationResponse;
import com.citizenscience.crowdsourceddata.dto.WaterObservationRequest.MeasurementsPayload;
import com.citizenscience.crowdsourceddata.model.ObservationCondition;
import com.citizenscience.crowdsourceddata.service.WaterObservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WaterObservationController.class)
class WaterObservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WaterObservationService service;

    @Test
    void postObservationReturnsCreated() throws Exception {
        WaterObservationResponse response = new WaterObservationResponse(
                "id", "citizen", "NE1", 10.0, 7.0, 30.0, 1.5,
                LocalDateTime.now(), Set.of(ObservationCondition.CLEAR), List.of("aW1hZ2U="));
        Mockito.when(service.saveObservation(eq("citizen"), any(WaterObservationRequest.class))).thenReturn(response);

        WaterObservationRequest request = new WaterObservationRequest();
        request.setPostcode("NE1");
        request.setTemperature(10.0);
        request.setObservations(List.of("Clear"));

        mockMvc.perform(post("/observations/citizen/citizen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void postObservationWithMeasurementsPayloadReturnsCreated() throws Exception {
        WaterObservationResponse response = new WaterObservationResponse(
                "id", "citizen", "NE1", 9.0, 6.8, null, null,
                LocalDateTime.now(), Set.of(ObservationCondition.CLEAR), List.of());
        Mockito.when(service.saveObservation(eq("citizen"), any(WaterObservationRequest.class))).thenReturn(response);

        MeasurementsPayload payload = new MeasurementsPayload();
        payload.setTemperature(9.0);
        payload.setPh(6.8);

        WaterObservationRequest request = new WaterObservationRequest();
        request.setPostcode("NE1");
        request.setMeasurements(payload);
        request.setObservations(List.of("Clear"));

        mockMvc.perform(post("/observations/citizen/citizen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void postObservationFailsValidation() throws Exception {
        WaterObservationRequest request = new WaterObservationRequest();
        request.setPostcode("");

        mockMvc.perform(post("/observations/citizen/citizen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postObservationForNewCitizenReturnsCreated() throws Exception {
        WaterObservationResponse response = new WaterObservationResponse(
                "id", "CTZ-001", "NE1", 11.0, null, null, null,
                LocalDateTime.now(), Set.of(ObservationCondition.CLEAR), List.of());
        Mockito.when(service.saveObservationForNewCitizen(any(WaterObservationRequest.class))).thenReturn(response);

        WaterObservationRequest request = new WaterObservationRequest();
        request.setPostcode("NE1");
        request.setObservations(List.of("Clear"));

        mockMvc.perform(post("/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getObservationsByCitizenReturnsOk() throws Exception {
        WaterObservationResponse response = new WaterObservationResponse(
                "id", "citizen", "NE1", null, null, null, null,
                LocalDateTime.now(), Set.of(ObservationCondition.CLEAR), List.of());
        Mockito.when(service.getObservationsByCitizenId("citizen")).thenReturn(List.of(response));

        mockMvc.perform(get("/observations/citizen/citizen"))
                .andExpect(status().isOk());
    }
}
