package com.citizenscience.crowdsourceddata.service;

import com.citizenscience.crowdsourceddata.dto.WaterObservationRequest;
import com.citizenscience.crowdsourceddata.dto.WaterObservationResponse;
import com.citizenscience.crowdsourceddata.exception.CitizenNotFoundException;
import com.citizenscience.crowdsourceddata.model.CitizenProfile;
import com.citizenscience.crowdsourceddata.model.ObservationCondition;
import com.citizenscience.crowdsourceddata.model.WaterObservation;
import com.citizenscience.crowdsourceddata.repository.WaterObservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core business service for citizen-submitted water observations.
 *
 * Validates payloads, enforces completeness rules, persists data, and exposes
 * read operations consumed by both the API gateway and the rewards service.
 * It also coordinates citizen registration when a submission arrives without
 * a pre-existing ID.
 */
@Service
public class WaterObservationService {

    /** Repository that persists each water observation record. */
    private final WaterObservationRepository repository;
    /** Service used to verify and create citizen identifiers. */
    private final CitizenService citizenService;

    /**
     * Builds the service with its data dependencies.
     *
     * @param repository persistence gateway for observations
     * @param citizenService collaborator for citizen lifecycle management
     */
    public WaterObservationService(WaterObservationRepository repository, CitizenService citizenService) {
        this.repository = repository;
        this.citizenService = citizenService;
    }

    /**
     * Stores an observation for an existing citizen.
     *
     * @param citizenId identifier supplied by the client path
     * @param request   validated submission data including postcode and readings
     * @return response DTO representing the persisted observation
     * @throws CitizenNotFoundException when the citizen cannot be located
     * @throws IllegalArgumentException when validation constraints fail
     */
    @Transactional
    public WaterObservationResponse saveObservation(String citizenId, WaterObservationRequest request) {
        validateRequest(request);

        if (citizenId == null || citizenId.isBlank()) {
            throw new IllegalArgumentException("Citizen ID is required");
        }

        String normalizedCitizenId = citizenId.trim();

        if (!citizenService.exists(normalizedCitizenId)) {
            throw new CitizenNotFoundException(citizenId);
        }

        WaterObservation observation = buildObservation(normalizedCitizenId, request);
        WaterObservation stored = repository.save(observation);
        return WaterObservationResponse.fromEntity(stored);
    }

    /**
     * Registers a citizen on the fly and persists their initial observation.
     *
     * @param request validated submission data including postcode and readings
     * @return response DTO including the generated citizen identifier
     * @throws IllegalArgumentException when validation constraints fail
     */
    @Transactional
    public WaterObservationResponse saveObservationForNewCitizen(WaterObservationRequest request) {
        validateRequest(request);

        CitizenProfile profile = citizenService.registerCitizen();
        WaterObservation observation = buildObservation(profile.getId(), request);
        WaterObservation stored = repository.save(observation);
        return WaterObservationResponse.fromEntity(stored);
    }

    /**
     * Retrieves a single observation by its unique identifier.
     *
     * @param id UUID of the persisted observation
     * @return mapped response representation of the record
     * @throws IllegalArgumentException when the observation cannot be found
     */
    @Transactional(readOnly = true)
    public WaterObservationResponse getObservation(String id) {
        return repository.findById(id)
                .map(WaterObservationResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found"));
    }

    /**
     * Fetches all observations associated with a specific citizen.
     *
     * @param citizenId identifier generated during registration
     * @return chronologically ordered list of observation responses
     * @throws CitizenNotFoundException when the citizen cannot be located
     * @throws IllegalArgumentException when the citizen ID is blank
     */
    @Transactional(readOnly = true)
    public List<WaterObservationResponse> getObservationsByCitizenId(String citizenId) {
        if (citizenId == null || citizenId.isBlank()) {
            throw new IllegalArgumentException("Citizen ID is required");
        }
        String normalizedCitizenId = citizenId.trim();
        if (!citizenService.exists(normalizedCitizenId)) {
            throw new CitizenNotFoundException(citizenId);
        }
        return repository.findByCitizenId(normalizedCitizenId).stream()
                .sorted(Comparator.comparing(WaterObservation::getSubmissionTimestamp))
                .map(WaterObservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns every observation currently stored in the database.
     *
     * @return list of all observation responses regardless of citizen
     */
    @Transactional(readOnly = true)
    public List<WaterObservationResponse> listAll() {
        return repository.findAll().stream()
                .map(WaterObservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateRequest(WaterObservationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Observation request is required");
        }

        if (request.getPostcode() == null || request.getPostcode().trim().isEmpty()) {
            throw new IllegalArgumentException("Postcode is required");
        }

        boolean hasObservation = !CollectionUtils.isEmpty(request.getObservations())
                && request.getObservations().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch(value -> !value.isEmpty());

        if (!CollectionUtils.isEmpty(request.getImages()) && request.getImages().size() > 3) {
            throw new IllegalArgumentException("A maximum of three images can be uploaded");
        }

        if (!hasObservation) {
            throw new IllegalArgumentException("At least one observation must be provided");
        }
    }

    private WaterObservation buildObservation(String citizenId, WaterObservationRequest request) {
        String normalizedPostcode = request.getPostcode().trim();
        List<String> normalizedObservations = request.getObservations() == null ? List.of()
                : request.getObservations().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(observation -> !observation.isEmpty())
                .collect(Collectors.toList());

        WaterObservation observation = new WaterObservation();
        observation.setCitizenId(citizenId);
        observation.setPostcode(normalizedPostcode);
        observation.setTemperature(request.getTemperature());
        observation.setPh(request.getPh());
        observation.setAlkalinity(request.getAlkalinity());
        observation.setTurbidity(request.getTurbidity());
        observation.setSubmissionTimestamp(LocalDateTime.now());

        Set<ObservationCondition> conditions = EnumSet.noneOf(ObservationCondition.class);
        if (!CollectionUtils.isEmpty(normalizedObservations)) {
            try {
                conditions = normalizedObservations.stream()
                        .map(ObservationCondition::fromString)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(ObservationCondition.class)));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("One or more observations are invalid", ex);
            }
        }
        observation.setConditions(conditions);

        List<String> images = request.getImages();
        if (!CollectionUtils.isEmpty(images)) {
            try {
                List<String> sanitizedImages = images.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(image -> !image.isEmpty())
                        .map(image -> {
                            Base64.getDecoder().decode(image);
                            return image;
                        })
                        .collect(Collectors.toList());
                observation.setImages(sanitizedImages);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Images must be Base64 encoded", ex);
            }
        } else {
            observation.setImages(List.of());
        }

        boolean hasAllMeasurements = request.getTemperature() != null
                && request.getPh() != null
                && request.getAlkalinity() != null
                && request.getTurbidity() != null;
        boolean hasObservation = !conditions.isEmpty();
        boolean hasImages = observation.getImages() != null && !observation.getImages().isEmpty();
        observation.setComplete(hasAllMeasurements && hasObservation && hasImages);

        return observation;
    }
}
