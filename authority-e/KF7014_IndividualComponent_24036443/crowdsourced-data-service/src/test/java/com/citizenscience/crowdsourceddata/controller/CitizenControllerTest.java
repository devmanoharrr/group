package com.citizenscience.crowdsourceddata.controller;

import com.citizenscience.crowdsourceddata.model.CitizenProfile;
import com.citizenscience.crowdsourceddata.service.CitizenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CitizenController.class)
class CitizenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitizenService citizenService;

    @Test
    void createCitizenReturnsIdentifier() throws Exception {
        CitizenProfile profile = new CitizenProfile("citizen-123", 1L, LocalDateTime.now());
        Mockito.when(citizenService.registerCitizen()).thenReturn(profile);

        mockMvc.perform(post("/citizens").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("citizen-123"));
    }
}
