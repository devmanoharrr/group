package com.waterquality.data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.waterquality.data.exception.CustomExceptions;
import com.waterquality.data.model.dto.ResponseDTOs;
import com.waterquality.data.model.dto.SubmissionRequest;
import com.waterquality.data.model.entity.WaterQualityObservation;
import com.waterquality.data.repository.WaterQualityObservationRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WaterQualityObservationService.
 * 
 * These tests verify the business logic and validation rules of the service layer
 * using mocked dependencies to ensure isolation and reliability.
 * 
 * Test Coverage:
 * - Successful observation submission
 * - Validation failures (missing data, invalid observations, too many images)
 * - Data retrieval operations
 * - Statistics calculation
 * - Processing status updates
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Water Quality Observation Service Tests")
class WaterQualityObservationServiceTest {

    @Mock
    private WaterQualityObservationRepository repository;

    @InjectMocks
    private WaterQualityObservationService service;

    private SubmissionRequest validRequest;
    private WaterQualityObservation savedObservation;

    @BeforeEach
    void setUp() {
        // Set up valid submission request with all fields
        validRequest = SubmissionRequest.builder()
                .citizenId("testUser123")
                .postcode("NE1 8ST")
                .measurements(SubmissionRequest.Measurements.builder()
                        .temperature(18.5)
                        .ph(7.2)
                        .alkalinity(120.0)
                        .turbidity(2.5)
                        .build())
                .observations(Arrays.asList("Clear", "Presence of Odour"))
                .images(Arrays.asList("image1.jpg", "image2.jpg"))
                .build();

        // Set up saved observation entity
        savedObservation = WaterQualityObservation.builder()
                .id("test-uuid-123")
                .citizenId("testUser123")
                .postcode("NE1 8ST")
                .temperature(18.5)
                .ph(7.2)
                .alkalinity(120.0)
                .turbidity(2.5)
                .observations("Clear,Presence of Odour")
                .images("image1.jpg,image2.jpg")
                .timestamp(LocalDateTime.now())
                .processed(false)
                .build();
    }

    @Test
    @DisplayName("Should successfully submit valid observation with all fields")
    void testSubmitObservation_Success_AllFields() {
        // Arrange
        when(repository.save(any(WaterQualityObservation.class))).thenReturn(savedObservation);

        // Act
        ResponseDTOs.SubmissionResponse response = service.submitObservation(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("accepted", response.getStatus());
        assertEquals("test-uuid-123", response.getId());
        assertNotNull(response.getTimestamp());
        verify(repository, times(1)).save(any(WaterQualityObservation.class));
    }

    @Test
    @DisplayName("Should successfully submit observation with only measurements")
    void testSubmitObservation_Success_OnlyMeasurements() {
        // Arrange
        SubmissionRequest requestWithOnlyMeasurements = SubmissionRequest.builder()
                .citizenId("testUser123")
                .postcode("NE1 8ST")
                .measurements(SubmissionRequest.Measurements.builder()
                        .temperature(18.5)
                        .build())
                .build();

        when(repository.save(any(WaterQualityObservation.class))).thenReturn(savedObservation);

        // Act
        ResponseDTOs.SubmissionResponse response = service.submitObservation(requestWithOnlyMeasurements);

        // Assert
        assertNotNull(response);
        assertEquals("accepted", response.getStatus());
        verify(repository, times(1)).save(any(WaterQualityObservation.class));
    }

    @Test
    @DisplayName("Should successfully submit observation with only observations")
    void testSubmitObservation_Success_OnlyObservations() {
        // Arrange
        SubmissionRequest requestWithOnlyObservations = SubmissionRequest.builder()
                .citizenId("testUser123")
                .postcode("NE1 8ST")
                .observations(Arrays.asList("Clear", "Cloudy"))
                .build();

        when(repository.save(any(WaterQualityObservation.class))).thenReturn(savedObservation);

        // Act
        ResponseDTOs.SubmissionResponse response = service.submitObservation(requestWithOnlyObservations);

        // Assert
        assertNotNull(response);
        assertEquals("accepted", response.getStatus());
        verify(repository, times(1)).save(any(WaterQualityObservation.class));
    }

    @Test
    @DisplayName("Should throw exception when no measurements or observations provided")
    void testSubmitObservation_Failure_NoData() {
        // Arrange
        SubmissionRequest invalidRequest = SubmissionRequest.builder()
                .citizenId("testUser123")
                .postcode("NE1 8ST")
                .build();

        // Act & Assert
        CustomExceptions.InvalidSubmissionException exception = assertThrows(
                CustomExceptions.InvalidSubmissionException.class,
                () -> service.submitObservation(invalidRequest)
        );

        assertTrue(exception.getMessage().contains("at least one measurement or one observation"));
        verify(repository, never()).save(any(WaterQualityObservation.class));
    }

    @Test
    @DisplayName("Should throw exception when invalid observation type provided")
    void testSubmitObservation_Failure_InvalidObservationType() {
        // Arrange
        SubmissionRequest invalidRequest = SubmissionRequest.builder()
                .citizenId("testUser123")
                .postcode("NE1 8ST")
                .observations(Arrays.asList("Clear", "InvalidType"))
                .build();

        // Act & Assert
        CustomExceptions.InvalidObservationTypeException exception = assertThrows(
                CustomExceptions.InvalidObservationTypeException.class,
                () -> service.submitObservation(invalidRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid observation type"));
        verify(repository, never()).save(any(WaterQualityObservation.class));
    }

    @Test
    @DisplayName("Should throw exception when more than 3 images provided")
    void testSubmitObservation_Failure_TooManyImages() {
        // Arrange
        SubmissionRequest invalidRequest = SubmissionRequest.builder()
                .citizenId("testUser123")
                .postcode("NE1 8ST")
                .observations(Arrays.asList("Clear"))
                .images(Arrays.asList("img1.jpg", "img2.jpg", "img3.jpg", "img4.jpg"))
                .build();

        // Act & Assert
        CustomExceptions.TooManyImagesException exception = assertThrows(
                CustomExceptions.TooManyImagesException.class,
                () -> service.submitObservation(invalidRequest)
        );

        assertTrue(exception.getMessage().contains("Maximum 3 images allowed"));
        verify(repository, never()).save(any(WaterQualityObservation.class));
    }

    @Test
    @DisplayName("Should retrieve all observations with limit")
    void testGetAllObservations_Success() {
        // Arrange
        List<WaterQualityObservation> observations = Arrays.asList(savedObservation);
        when(repository.findAllOrderByTimestampDesc()).thenReturn(observations);

        // Act
        List<ResponseDTOs.ObservationResponse> result = service.getAllObservations(100);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-uuid-123", result.get(0).getId());
        verify(repository, times(1)).findAllOrderByTimestampDesc();
    }

    @Test
    @DisplayName("Should retrieve observations by citizen ID")
    void testGetObservationsByCitizen_Success() {
        // Arrange
        List<WaterQualityObservation> observations = Arrays.asList(savedObservation);
        when(repository.findByCitizenIdOrderByTimestampDesc("testUser123")).thenReturn(observations);

        // Act
        List<ResponseDTOs.ObservationResponse> result = service.getObservationsByCitizen("testUser123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testUser123", result.get(0).getCitizenId());
        verify(repository, times(1)).findByCitizenIdOrderByTimestampDesc("testUser123");
    }

    @Test
    @DisplayName("Should retrieve observation by ID")
    void testGetObservationById_Success() {
        // Arrange
        when(repository.findById("test-uuid-123")).thenReturn(Optional.of(savedObservation));

        // Act
        ResponseDTOs.ObservationResponse result = service.getObservationById("test-uuid-123");

        // Assert
        assertNotNull(result);
        assertEquals("test-uuid-123", result.getId());
        verify(repository, times(1)).findById("test-uuid-123");
    }

    @Test
    @DisplayName("Should throw exception when observation not found by ID")
    void testGetObservationById_NotFound() {
        // Arrange
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        CustomExceptions.ObservationNotFoundException exception = assertThrows(
                CustomExceptions.ObservationNotFoundException.class,
                () -> service.getObservationById("non-existent")
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository, times(1)).findById("non-existent");
    }

    @Test
    @DisplayName("Should retrieve correct statistics")
    void testGetStatistics_Success() {
        // Arrange
        when(repository.count()).thenReturn(100L);
        when(repository.countByProcessed(true)).thenReturn(70L);

        // Act
        ResponseDTOs.StatsResponse result = service.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotal());
        assertEquals(70L, result.getProcessed());
        assertEquals(30L, result.getUnprocessed());
        verify(repository, times(1)).count();
        verify(repository, times(1)).countByProcessed(true);
    }

    @Test
    @DisplayName("Should mark observation as processed")
    void testMarkAsProcessed_Success() {
        // Arrange
        when(repository.findById("test-uuid-123")).thenReturn(Optional.of(savedObservation));
        when(repository.save(any(WaterQualityObservation.class))).thenReturn(savedObservation);

        // Act
        service.markAsProcessed("test-uuid-123");

        // Assert
        verify(repository, times(1)).findById("test-uuid-123");
        verify(repository, times(1)).save(any(WaterQualityObservation.class));
        assertTrue(savedObservation.getProcessed());
    }

    @Test
    @DisplayName("Should throw exception when marking non-existent observation as processed")
    void testMarkAsProcessed_NotFound() {
        // Arrange
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        CustomExceptions.ObservationNotFoundException exception = assertThrows(
                CustomExceptions.ObservationNotFoundException.class,
                () -> service.markAsProcessed("non-existent")
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository, times(1)).findById("non-existent");
        verify(repository, never()).save(any(WaterQualityObservation.class));
    }
}
