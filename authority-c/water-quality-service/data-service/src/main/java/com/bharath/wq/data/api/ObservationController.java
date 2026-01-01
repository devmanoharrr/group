package com.bharath.wq.data.api;

import com.bharath.wq.data.api.dto.CreateObservationRequest;
import com.bharath.wq.data.api.dto.CreateObservationResult;
import com.bharath.wq.data.api.dto.ObservationResponse;
import com.bharath.wq.data.service.ObservationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST controller for managing water quality observations.
 *
 * <p>Provides endpoints for creating and retrieving observations. All endpoints return JSON
 * responses.
 */
@RestController
@RequestMapping("/observations")
public class ObservationController {

  private final ObservationService service;

  /**
   * Constructs a new ObservationController.
   *
   * @param service the observation service
   */
  public ObservationController(ObservationService service) {
    this.service = service;
  }

  /**
   * Creates a new observation.
   *
   * @param request the observation creation request (must be valid)
   * @param uriBuilder URI builder for constructing the location header
   * @return ResponseEntity with status 201 (Created) and the observation ID
   * @throws jakarta.validation.ConstraintViolationException if validation fails (400 Bad Request)
   */
  @PostMapping
  public ResponseEntity<CreateObservationResult> create(
      @Valid @RequestBody CreateObservationRequest request, UriComponentsBuilder uriBuilder) {
    final String id = service.create(request);
    final URI location = ObservationService.createdLocation(uriBuilder, id);
    return ResponseEntity.created(location).body(new CreateObservationResult(id));
  }

  /**
   * Retrieves the latest observations.
   *
   * @param authority optional authority filter (e.g., "NE")
   * @param limit maximum number of observations to return (default: 5, clamped between 1 and 50)
   * @return list of observation responses, ordered by creation time (newest first)
   */
  @GetMapping("/latest")
  public List<ObservationResponse> latest(
      @RequestParam(value = "authority", required = false) String authority,
      @RequestParam(value = "limit", defaultValue = "5") int limit) {
    final int safe = Math.max(1, Math.min(limit, 50));
    return service.latest(authority, safe);
  }

  /**
   * Counts observations, optionally filtered by authority.
   *
   * @param authority optional authority filter (e.g., "NE")
   * @return the total count of observations matching the filter
   */
  @GetMapping("/count")
  public long count(@RequestParam(value = "authority", required = false) String authority) {
    return service.count(authority);
  }

  /**
   * Retrieve a specific observation by its ID.
   *
   * @param id the observation ID
   * @return the observation response
   * @throws ObservationNotFoundException if the observation is not found (returns 404)
   */
  @GetMapping("/{id}")
  public ObservationResponse getById(@PathVariable("id") String id) {
    return service.getById(id);
  }
}
