package com.waterquality.data.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.waterquality.data.model.dto.ResponseDTOs;
import com.waterquality.data.model.dto.SubmissionRequest;
import com.waterquality.data.service.WaterQualityObservationService;

import java.util.List;

/**
 * REST Controller for the Crowdsourced Data Service.
 * 
 * This controller provides RESTful API endpoints for submitting, retrieving, and
 * managing citizen-submitted water quality observations.
 * 
 * Base path: /api/data
 * 
 * Endpoints:
 * - POST   /api/data/submit         : Submit a new observation
 * - GET    /api/data/observations   : Get all observations (with optional limit)
 * - GET    /api/data/observations/{id} : Get a specific observation by ID
 * - GET    /api/data/citizen/{citizenId} : Get observations for a citizen
 * - GET    /api/data/stats          : Get statistics
 * - PUT    /api/data/{id}/process   : Mark observation as processed
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class WaterQualityObservationController {

    private final WaterQualityObservationService service;

    /**
     * Submit a new water quality observation.
     * 
     * Accepts a JSON payload with observation data and validates it against
     * the requirements (postcode + at least one measurement or observation).
     * 
     * @param request the submission request containing observation data
     * @return ResponseEntity with submission response (201 Created)
     * 
     * Example Request:
     * POST /api/data/submit
     * {
     *   "citizenId": "user123",
     *   "postcode": "NE1 8ST",
     *   "measurements": {
     *     "temperature": 18.5,
     *     "ph": 7.2,
     *     "alkalinity": 120.0,
     *     "turbidity": 2.5
     *   },
     *   "observations": ["Clear", "No unusual odor"],
     *   "images": ["image1.jpg", "image2.jpg"]
     * }
     */
    @PostMapping("/submit")
    public ResponseEntity<ResponseDTOs.SubmissionResponse> submitObservation(
            @Valid @RequestBody SubmissionRequest request) {
        
        log.info("Received observation submission from citizen: {}", request.getCitizenId());
        
        ResponseDTOs.SubmissionResponse response = service.submitObservation(request);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all observations with optional limit.
     * 
     * Returns a list of all observations ordered by most recent first.
     * Default limit is 100 observations.
     * 
     * @param limit maximum number of observations to return (optional, default 100)
     * @return ResponseEntity with list of observations (200 OK)
     * 
     * Example Request:
     * GET /api/data/observations?limit=50
     */
    @GetMapping("/observations")
    public ResponseEntity<List<ResponseDTOs.ObservationResponse>> getAllObservations(
            @RequestParam(defaultValue = "100") int limit) {
        
        log.info("Fetching all observations with limit: {}", limit);
        
        List<ResponseDTOs.ObservationResponse> observations = service.getAllObservations(limit);
        
        return ResponseEntity.ok(observations);
    }

    /**
     * Get a specific observation by ID.
     * 
     * @param id the observation UUID
     * @return ResponseEntity with observation details (200 OK)
     * @throws CustomExceptions.ObservationNotFoundException if not found (404 Not Found)
     * 
     * Example Request:
     * GET /api/data/observations/550e8400-e29b-41d4-a716-446655440000
     */
    @GetMapping("/observations/{id}")
    public ResponseEntity<ResponseDTOs.ObservationResponse> getObservationById(
            @PathVariable String id) {
        
        log.info("Fetching observation with ID: {}", id);
        
        ResponseDTOs.ObservationResponse observation = service.getObservationById(id);
        
        return ResponseEntity.ok(observation);
    }

    /**
     * Get all observations for a specific citizen.
     * 
     * @param citizenId the citizen's unique identifier
     * @return ResponseEntity with list of citizen's observations (200 OK)
     * 
     * Example Request:
     * GET /api/data/citizen/user123
     */
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<ResponseDTOs.ObservationResponse>> getObservationsByCitizen(
            @PathVariable String citizenId) {
        
        log.info("Fetching observations for citizen: {}", citizenId);
        
        List<ResponseDTOs.ObservationResponse> observations = 
                service.getObservationsByCitizen(citizenId);
        
        return ResponseEntity.ok(observations);
    }

    /**
     * Get statistics about observations.
     * 
     * Returns total count, processed count, and unprocessed count.
     * 
     * @return ResponseEntity with statistics (200 OK)
     * 
     * Example Request:
     * GET /api/data/stats
     * 
     * Example Response:
     * {
     *   "total": 150,
     *   "processed": 120,
     *   "unprocessed": 30
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<ResponseDTOs.StatsResponse> getStatistics() {
        
        log.info("Fetching observation statistics");
        
        ResponseDTOs.StatsResponse stats = service.getStatistics();
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Mark an observation as processed.
     * 
     * This endpoint is called by the Rewards Service after processing an observation
     * to award points to the citizen.
     * 
     * @param id the observation UUID
     * @return ResponseEntity with success message (200 OK)
     * @throws CustomExceptions.ObservationNotFoundException if not found (404 Not Found)
     * 
     * Example Request:
     * PUT /api/data/550e8400-e29b-41d4-a716-446655440000/process
     */
    @PutMapping("/{id}/process")
    public ResponseEntity<String> markAsProcessed(@PathVariable String id) {
        
        log.info("Marking observation as processed: {}", id);
        
        service.markAsProcessed(id);
        
        return ResponseEntity.ok("Observation marked as processed");
    }

    /**
     * Health check endpoint.
     * 
     * @return ResponseEntity with status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Crowdsourced Data Service is running");
    }
}
