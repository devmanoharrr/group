package com.bharath.wq.data.service;

import com.bharath.wq.data.api.dto.CreateObservationRequest;
import com.bharath.wq.data.api.dto.ObservationResponse;
import com.bharath.wq.data.repo.ObservationRepository;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service layer for managing water quality observations.
 *
 * <p>This service handles the business logic for creating, retrieving, and querying observations.
 * It coordinates between the API layer and the persistence layer.
 */
@Service
public class ObservationService {

  private final ObservationRepository repo;

  /**
   * Constructs a new ObservationService.
   *
   * @param repo the observation repository for data access
   */
  public ObservationService(ObservationRepository repo) {
    this.repo = repo;
  }

  /**
   * Creates a new observation from the provided request.
   *
   * <p>Generates a unique ID and timestamp, sanitizes image paths, and stores the observation in
   * the database.
   *
   * @param req the observation creation request
   * @return the generated observation ID
   */
  public String create(CreateObservationRequest req) {
    final String id = UUID.randomUUID().toString();
    final Instant ts = Instant.now();
    final var safeImages = ImagePathSanitizer.sanitize(req.imagePaths());
    final String auth = req.authority() == null ? null : req.authority().trim().toUpperCase();

    final ObservationRecord r =
        new ObservationRecord(
            id,
            req.citizenId(),
            req.postcode(),
            req.temperatureC(),
            req.pH(),
            req.alkalinityMgL(),
            req.turbidityNTU(),
            req.observations(),
            safeImages,
            (auth != null && !auth.isEmpty()) ? auth : null,
            ts);

    repo.insert(r);
    return id;
  }

  /**
   * Retrieves the latest observations, optionally filtered by authority.
   *
   * @param authority optional authority filter (e.g., "NE")
   * @param limit maximum number of observations to return (clamped between 1 and 50)
   * @return list of observation responses, ordered by creation time (newest first)
   */
  public List<ObservationResponse> latest(String authority, int limit) {
    return repo.findLatest(authority, limit).stream()
        .map(ObservationService::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Counts observations, optionally filtered by authority.
   *
   * @param authority optional authority filter (e.g., "NE"), or null for all observations
   * @return the total count of observations matching the filter
   */
  public long count(String authority) {
    return repo.countByAuthority(authority);
  }

  /**
   * Retrieve an observation by its ID.
   *
   * @param id the observation ID
   * @return the observation response
   * @throws com.bharath.wq.data.api.ObservationNotFoundException if the observation is not found
   */
  public ObservationResponse getById(String id) {
    final ObservationRecord record = repo.findById(id);
    if (record == null) {
      throw new com.bharath.wq.data.api.ObservationNotFoundException(
          "Observation not found with id: " + id);
    }
    return toResponse(record);
  }

  /**
   * Builds the location URI for a newly created observation.
   *
   * @param base the URI components builder
   * @param id the observation ID
   * @return the location URI
   */
  public static URI createdLocation(UriComponentsBuilder base, String id) {
    return base.path("/observations/{id}").build(id);
  }

  private static ObservationResponse toResponse(ObservationRecord r) {
    return new ObservationResponse(
        r.id(),
        r.citizenId(),
        r.postcode(),
        r.temperatureC(),
        r.pH(),
        r.alkalinityMgL(),
        r.turbidityNTU(),
        r.observations(),
        r.imagePaths(),
        r.authority(),
        r.createdAt());
  }
}
