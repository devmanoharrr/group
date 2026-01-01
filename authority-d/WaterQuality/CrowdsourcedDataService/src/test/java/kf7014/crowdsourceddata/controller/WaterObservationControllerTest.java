package kf7014.crowdsourceddata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import kf7014.crowdsourceddata.model.MeasurementSet;
import kf7014.crowdsourceddata.model.WaterObservation;
import kf7014.crowdsourceddata.service.WaterObservationService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WaterObservationController.class)
class WaterObservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WaterObservationService service;

    @Test
    void submit_withValidObservation_returnsCreated() throws Exception {
        WaterObservation request = createValidObservation();
        WaterObservation saved = createValidObservation();
        // Note: WaterObservation doesn't have setId() - id is auto-generated in constructor

        Mockito.when(service.createObservation(Mockito.any())).thenReturn(saved);

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.citizenId", is("citizen-001")))
                .andExpect(jsonPath("$.postcode", is("NE1 1AA")));
    }

    @Test
    void submit_withMissingPostcode_returnsBadRequest() throws Exception {
        WaterObservation request = createValidObservation();
        request.setPostcode(null);

        Mockito.when(service.createObservation(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Postcode is required"));

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Postcode is required")));
    }

    @Test
    void submit_withMissingCitizenId_returnsBadRequest() throws Exception {
        WaterObservation request = createValidObservation();
        request.setCitizenId(null);

        Mockito.when(service.createObservation(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Citizen ID is required"));

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Citizen ID is required")));
    }

    @Test
    void submit_withNoMeasurementsAndNoObservations_returnsBadRequest() throws Exception {
        WaterObservation request = createValidObservation();
        request.setMeasurements(null);
        request.setObservations(null);

        Mockito.when(service.createObservation(Mockito.any()))
                .thenThrow(new IllegalArgumentException("At least one measurement or observation is required"));

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_withOnlyMeasurements_returnsCreated() throws Exception {
        WaterObservation request = createValidObservation();
        request.setObservations(null);

        Mockito.when(service.createObservation(Mockito.any())).thenReturn(request);

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void submit_withOnlyObservations_returnsCreated() throws Exception {
        WaterObservation request = createValidObservation();
        request.setMeasurements(null);

        Mockito.when(service.createObservation(Mockito.any())).thenReturn(request);

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void submit_withInvalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_withMissingContentType_returnsUnsupportedMediaType() throws Exception {
        WaterObservation request = createValidObservation();

        mockMvc.perform(post("/api/observations")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void list_withNoCitizenId_returnsAllObservations() throws Exception {
        List<WaterObservation> observations = List.of(createValidObservation());
        Mockito.when(service.listAll()).thenReturn(observations);

        mockMvc.perform(get("/api/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(1)));

        Mockito.verify(service).listAll();
        Mockito.verify(service, never()).listByCitizen(anyString());
    }

    @Test
    void list_withCitizenId_returnsFilteredObservations() throws Exception {
        List<WaterObservation> observations = List.of(createValidObservation());
        Mockito.when(service.listByCitizen("citizen-001")).thenReturn(observations);

        mockMvc.perform(get("/api/observations")
                        .param("citizenId", "citizen-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(1)));

        Mockito.verify(service).listByCitizen("citizen-001");
        Mockito.verify(service, never()).listAll();
    }

    @Test
    void list_withEmptyCitizenId_returnsAllObservations() throws Exception {
        List<WaterObservation> observations = List.of(createValidObservation());
        Mockito.when(service.listAll()).thenReturn(observations);

        mockMvc.perform(get("/api/observations")
                        .param("citizenId", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Mockito.verify(service).listAll();
    }

    @Test
    void list_withBlankCitizenId_returnsAllObservations() throws Exception {
        List<WaterObservation> observations = List.of(createValidObservation());
        Mockito.when(service.listAll()).thenReturn(observations);

        mockMvc.perform(get("/api/observations")
                        .param("citizenId", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Mockito.verify(service).listAll();
    }

    @Test
    void list_withNoObservations_returnsEmptyArray() throws Exception {
        Mockito.when(service.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void list_withMultipleObservations_returnsAll() throws Exception {
        List<WaterObservation> observations = List.of(
                createValidObservation(),
                createValidObservation()
        );
        Mockito.when(service.listAll()).thenReturn(observations);

        mockMvc.perform(get("/api/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
    }

    @Test
    void submit_withFullMeasurementSet_returnsCreated() throws Exception {
        WaterObservation request = createValidObservation();
        MeasurementSet measurements = new MeasurementSet();
        measurements.setTemperatureCelsius(20.0);
        measurements.setPh(7.0);
        measurements.setAlkalinityMgPerL(100.0);
        measurements.setTurbidityNtu(5.0);
        request.setMeasurements(measurements);

        Mockito.when(service.createObservation(Mockito.any())).thenReturn(request);

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void submit_withMultipleObservations_returnsCreated() throws Exception {
        WaterObservation request = createValidObservation();
        request.setObservations(List.of("Clear", "Cloudy", "Murky"));

        Mockito.when(service.createObservation(Mockito.any())).thenReturn(request);

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void submit_withImageData_returnsCreated() throws Exception {
        WaterObservation request = createValidObservation();
        request.setImageData(List.of("img1", "img2", "img3"));

        Mockito.when(service.createObservation(Mockito.any())).thenReturn(request);

        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private WaterObservation createValidObservation() {
        WaterObservation observation = new WaterObservation();
        observation.setCitizenId("citizen-001");
        observation.setPostcode("NE1 1AA");
        MeasurementSet measurements = new MeasurementSet();
        measurements.setPh(7.0);
        observation.setMeasurements(measurements);
        observation.setObservations(List.of("Clear"));
        return observation;
    }
}



