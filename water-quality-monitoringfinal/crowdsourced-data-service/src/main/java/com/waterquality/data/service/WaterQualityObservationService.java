package com.waterquality.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.waterquality.data.exception.CustomExceptions;
import com.waterquality.data.model.dto.ResponseDTOs;
import com.waterquality.data.model.dto.SubmissionRequest;
import com.waterquality.data.model.entity.WaterQualityObservation;
import com.waterquality.data.repository.WaterQualityObservationRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing water quality observations.
 * 
 * This service contains the business logic for validating, processing, and storing
 * citizen-submitted water quality observations. It acts as an intermediary between
 * the controller layer and the data access layer.
 * 
 * Key responsibilities:
 * - Validate submission requests
 * - Transform DTOs to entities and vice versa
 * - Perform business logic operations
 * - Interact with the repository layer
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaterQualityObservationService {

    private final WaterQualityObservationRepository repository;

    /**
     * Valid observation types as per requirements.
     */
    private static final List<String> VALID_OBSERVATIONS = Arrays.asList(
            "Clear", "Cloudy", "Murky", "Foamy", "Oily", "Discoloured", "Presence of Odour"
    );

    /**
     * Maximum number of images allowed per submission.
     */
    private static final int MAX_IMAGES = 3;

    /**
     * Submits a new water quality observation.
     * 
     * Validates the submission to ensure it contains:
     * - Postcode (already validated by @NotBlank)
     * - At least one measurement OR one observation
     * - Valid observation types (if provided)
     * - Maximum 3 images (if provided)
     * 
     * @param request the submission request containing observation data
     * @return SubmissionResponse with generated ID and timestamp
     * @throws CustomExceptions.InvalidSubmissionException if validation fails
     * @throws CustomExceptions.TooManyImagesException if more than 3 images
     * @throws CustomExceptions.InvalidObservationTypeException if invalid observation type
     */
    @Transactional
    public ResponseDTOs.SubmissionResponse submitObservation(SubmissionRequest request) {
        log.info("Processing observation submission from citizen: {}", request.getCitizenId());

        // Validate submission has at least one measurement or observation
        validateSubmission(request);

        // Validate observations are from allowed list
        if (request.getObservations() != null && !request.getObservations().isEmpty()) {
            validateObservationTypes(request.getObservations());
        }

        // Validate image count
        if (request.getImages() != null && request.getImages().size() > MAX_IMAGES) {
            throw new CustomExceptions.TooManyImagesException(
                    "Maximum " + MAX_IMAGES + " images allowed per submission");
        }

        // Convert DTO to Entity
        WaterQualityObservation observation = convertToEntity(request);

        // Save to database
        try {
            WaterQualityObservation saved = repository.save(observation);
            log.info("Observation saved successfully with ID: {}", saved.getId());

            return ResponseDTOs.SubmissionResponse.builder()
                    .status("accepted")
                    .id(saved.getId())
                    .timestamp(saved.getTimestamp())
                    .message("Observation submitted successfully")
                    .build();
        } catch (Exception e) {
            log.error("Failed to save observation", e);
            throw new CustomExceptions.DatabaseException("Failed to save observation", e);
        }
    }

    /**
     * Retrieves all observations, ordered by most recent first.
     * 
     * @param limit maximum number of observations to return
     * @return list of observation responses
     */
    @Transactional(readOnly = true)
    public List<ResponseDTOs.ObservationResponse> getAllObservations(int limit) {
        log.info("Retrieving all observations with limit: {}", limit);

        List<WaterQualityObservation> observations = repository.findAllOrderByTimestampDesc();

        return observations.stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves observations for a specific citizen.
     * 
     * @param citizenId the citizen's unique identifier
     * @return list of observation responses
     */
    @Transactional(readOnly = true)
    public List<ResponseDTOs.ObservationResponse> getObservationsByCitizen(String citizenId) {
        log.info("Retrieving observations for citizen: {}", citizenId);

        List<WaterQualityObservation> observations = 
                repository.findByCitizenIdOrderByTimestampDesc(citizenId);

        return observations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single observation by ID.
     * 
     * @param id the observation ID
     * @return observation response
     * @throws CustomExceptions.ObservationNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public ResponseDTOs.ObservationResponse getObservationById(String id) {
        log.info("Retrieving observation with ID: {}", id);

        WaterQualityObservation observation = repository.findById(id)
                .orElseThrow(() -> new CustomExceptions.ObservationNotFoundException(
                        "Observation not found with ID: " + id));

        return convertToResponse(observation);
    }

    /**
     * Retrieves statistics about observations.
     * 
     * @return stats response with counts
     */
    @Transactional(readOnly = true)
    public ResponseDTOs.StatsResponse getStatistics() {
        log.info("Retrieving observation statistics");

        long total = repository.count();
        long processed = repository.countByProcessed(true);
        long unprocessed = total - processed;

        return ResponseDTOs.StatsResponse.builder()
                .total(total)
                .processed(processed)
                .unprocessed(unprocessed)
                .build();
    }

    /**
     * Marks an observation as processed (called by Rewards Service).
     * 
     * @param id the observation ID
     */
    @Transactional
    public void markAsProcessed(String id) {
        log.info("Marking observation as processed: {}", id);

        WaterQualityObservation observation = repository.findById(id)
                .orElseThrow(() -> new CustomExceptions.ObservationNotFoundException(
                        "Observation not found with ID: " + id));

        observation.setProcessed(true);
        repository.save(observation);
    }

    /**
     * Validates that the submission meets minimum requirements.
     * 
     * @param request the submission request
     * @throws CustomExceptions.InvalidSubmissionException if validation fails
     */
    private void validateSubmission(SubmissionRequest request) {
        boolean hasMeasurement = request.getMeasurements() != null && 
                                 request.getMeasurements().hasAnyValue();
        boolean hasObservation = request.getObservations() != null && 
                                !request.getObservations().isEmpty();

        if (!hasMeasurement && !hasObservation) {
            throw new CustomExceptions.InvalidSubmissionException(
                    "Submission must include at least one measurement or one observation");
        }
    }

    /**
     * Validates that all observation types are from the allowed list.
     * 
     * @param observations list of observation types
     * @throws CustomExceptions.InvalidObservationTypeException if invalid type found
     */
    private void validateObservationTypes(List<String> observations) {
        for (String obs : observations) {
            if (!VALID_OBSERVATIONS.contains(obs)) {
                throw new CustomExceptions.InvalidObservationTypeException(
                        "Invalid observation type: " + obs + ". Allowed values: " + 
                        String.join(", ", VALID_OBSERVATIONS));
            }
        }
    }

    /**
     * Converts a SubmissionRequest DTO to a WaterQualityObservation entity.
     * 
     * @param request the submission request
     * @return the entity
     */
    private WaterQualityObservation convertToEntity(SubmissionRequest request) {
        WaterQualityObservation.WaterQualityObservationBuilder builder = 
                WaterQualityObservation.builder()
                .citizenId(request.getCitizenId())
                .postcode(request.getPostcode());

        if (request.getMeasurements() != null) {
            builder.temperature(request.getMeasurements().getTemperature())
                   .ph(request.getMeasurements().getPh())
                   .alkalinity(request.getMeasurements().getAlkalinity())
                   .turbidity(request.getMeasurements().getTurbidity());
        }

        if (request.getObservations() != null && !request.getObservations().isEmpty()) {
            builder.observations(String.join(",", request.getObservations()));
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            builder.images(String.join(",", request.getImages()));
        }

        return builder.build();
    }

    /**
     * Converts a WaterQualityObservation entity to an ObservationResponse DTO.
     * 
     * @param observation the entity
     * @return the response DTO
     */
    private ResponseDTOs.ObservationResponse convertToResponse(WaterQualityObservation observation) {
        List<String> observationsList = observation.getObservations() != null ?
                Arrays.asList(observation.getObservations().split(",")) : null;

        List<String> imagesList = observation.getImages() != null ?
                Arrays.asList(observation.getImages().split(",")) : null;

        return ResponseDTOs.ObservationResponse.builder()
                .id(observation.getId())
                .citizenId(observation.getCitizenId())
                .postcode(observation.getPostcode())
                .temperature(observation.getTemperature())
                .ph(observation.getPh())
                .alkalinity(observation.getAlkalinity())
                .turbidity(observation.getTurbidity())
                .observations(observationsList)
                .images(imagesList)
                .timestamp(observation.getTimestamp())
                .processed(observation.getProcessed())
                .build();
    }
}
