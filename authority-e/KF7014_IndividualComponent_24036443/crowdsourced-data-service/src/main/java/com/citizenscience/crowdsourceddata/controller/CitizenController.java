package com.citizenscience.crowdsourceddata.controller;

import com.citizenscience.crowdsourceddata.dto.CitizenResponse;
import com.citizenscience.crowdsourceddata.model.CitizenProfile;
import com.citizenscience.crowdsourceddata.service.CitizenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes citizen registration endpoints.
 *
 * This controller provides the workflow that allocates sequential citizen
 * identifiers (e.g. {@code CTZ-001}) before any observations are submitted.
 * Clients typically call this endpoint once, store the identifier, and use it
 * for all subsequent observation requests.
 */
@RestController
@RequestMapping("/citizens")
public class CitizenController {

    /** Handles registration and persistence of citizen profiles. */
    private final CitizenService citizenService;

    /**
     * Creates a controller with injected business logic.
     *
     * @param citizenService service responsible for generating citizen IDs
     */
    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    /**
     * Registers a new citizen and returns their generated identifier.
     *
     * <p>Example response:</p>
     * <pre>
     * {
     *   "citizenId": "CTZ-001",
     *   "createdAt": "2024-03-25T09:12:30"
     * }
     * </pre>
     *
     * @return a {@link CitizenResponse} with the sequential citizen identifier
     */
    @PostMapping
    public ResponseEntity<CitizenResponse> createCitizen() {
        CitizenProfile profile = citizenService.registerCitizen();
        CitizenResponse response = new CitizenResponse(profile.getId(), profile.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
