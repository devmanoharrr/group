package com.citizenscience.crowdsourceddata.controller;

import com.citizenscience.crowdsourceddata.dto.WaterObservationRequest;
import com.citizenscience.crowdsourceddata.dto.WaterObservationResponse;
import com.citizenscience.crowdsourceddata.service.WaterObservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for citizen water quality observations.
 *
 * Handles citizen registration submissions, follow-up updates, and read
 * operations. The controller is the primary entry point for the API gateway
 * when citizens upload new measurements or when the rewards microservice polls
 * historic data.
 */
@RestController
@RequestMapping("/observations")
public class WaterObservationController {

    /** Coordinates validation, persistence, and response mapping. */
    private final WaterObservationService service;

    /**
     * Builds a controller with the required service dependency.
     *
     * @param service business logic for handling observations
     */
    public WaterObservationController(WaterObservationService service) {
        this.service = service;
    }

    /**
     * Creates a new citizen and stores their first observation in a single call.
     *
     * <p>Example request:</p>
     * <pre>
     * {
     *   "postcode": "NE1 4LP",
     *   "measurements": {"temperature": 12.5},
     *   "observations": ["Clear"]
     * }
     * </pre>
     *
     * @param request validated payload describing the observation details
     * @return the stored observation alongside the generated citizen ID
     */
    @PostMapping
    public ResponseEntity<WaterObservationResponse> createObservationForNewCitizen(
            @Valid @RequestBody WaterObservationRequest request) {
        WaterObservationResponse response = service.saveObservationForNewCitizen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Records an observation for an existing citizen.
     *
     * @param citizenId identifier generated during registration
     * @param request   validated payload describing the observation details
     * @return the stored observation mapped to a response DTO
     */
    @PostMapping("/citizen/{citizenId}")
    public ResponseEntity<WaterObservationResponse> createObservation(@PathVariable String citizenId,
                                                                      @Valid @RequestBody WaterObservationRequest request) {
        WaterObservationResponse response = service.saveObservation(citizenId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a single observation by its UUID.
     *
     * @param id generated identifier of the observation record
     * @return the observation if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<WaterObservationResponse> getObservation(@PathVariable String id) {
        return ResponseEntity.ok(service.getObservation(id));
    }

    /**
     * Lists all stored observations regardless of citizen.
     *
     * @return a collection of every persisted observation
     */
    @GetMapping
    public ResponseEntity<List<WaterObservationResponse>> listObservations() {
        return ResponseEntity.ok(service.listAll());
    }

    /**
     * Fetches every observation submitted by a particular citizen.
     *
     * <p>The rewards microservice polls this endpoint to build up point totals.</p>
     *
     * @param citizenId identifier allocated by {@link CitizenController}
     * @return a list of the citizen's observations ordered by submission time
     */
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<WaterObservationResponse>> getObservationsByCitizen(@PathVariable String citizenId) {
        List<WaterObservationResponse> responses = service.getObservationsByCitizenId(citizenId);
        return ResponseEntity.ok(responses);
    }
}
