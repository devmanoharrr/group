package com.bharath.wq.data.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bharath.wq.data.api.dto.CreateObservationRequest;
import com.bharath.wq.data.model.ObservationTag;
import com.bharath.wq.data.service.ObservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ObservationController.class)
@Import(ApiErrorAdvice.class)
class ObservationControllerValidationTest {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper om;

  @MockBean private ObservationService service;

  @Test
  void post_should400_whenNoMeasurementsAndNoObservations() throws Exception {
    var req =
        new CreateObservationRequest("c1", "NE1 4LP", null, null, null, null, null, null, "NE");
    mvc.perform(
            post("/observations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void post_should400_whenBadPostcode() throws Exception {
    var req =
        new CreateObservationRequest(
            "c1", "12345", null, null, null, null, Set.of(ObservationTag.CLEAR), null, "NE");
    mvc.perform(
            post("/observations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void post_should400_whenTooManyImages() throws Exception {
    var req =
        new CreateObservationRequest(
            "c1",
            "NE1 4LP",
            null,
            7.1,
            null,
            null,
            Set.of(),
            List.of("a.jpg", "b.jpg", "c.jpg", "d.jpg"),
            "NE");
    mvc.perform(
            post("/observations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void post_should201_whenValidMinimal() throws Exception {
    given(service.create(any(CreateObservationRequest.class))).willReturn("abc-123");
    var req =
        new CreateObservationRequest(
            "c1",
            "NE1 4LP",
            null,
            null,
            null,
            null,
            Set.of(ObservationTag.CLEAR),
            List.of("images/pic.jpg"),
            "ne");
    mvc.perform(
            post("/observations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/observations/abc-123"))
        .andExpect(jsonPath("$.id").value("abc-123"));
  }

  @Test
  void get_latest_shouldLimitBetween1and50() throws Exception {
    Mockito.when(service.latest(null, 5)).thenReturn(List.of());
    mvc.perform(get("/observations/latest?limit=0")).andExpect(status().isOk());
    mvc.perform(get("/observations/latest?limit=999")).andExpect(status().isOk());
  }
}
