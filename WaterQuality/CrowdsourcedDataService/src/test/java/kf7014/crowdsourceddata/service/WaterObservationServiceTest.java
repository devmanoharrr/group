package kf7014.crowdsourceddata.service;

import kf7014.crowdsourceddata.model.MeasurementSet;
import kf7014.crowdsourceddata.model.WaterObservation;
import kf7014.crowdsourceddata.repository.WaterObservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaterObservationServiceTest {

    @Mock
    private WaterObservationRepository repository;

    private WaterObservationService service;

    @BeforeEach
    void setUp() {
        service = new WaterObservationService(repository);
    }

    @Test
    void createObservation_withValidObservation_returnsSaved() {
        WaterObservation observation = createValidObservation();
        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        verify(repository).save(observation);
    }

    @Test
    void createObservation_withNullPostcode_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setPostcode(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("Postcode is required", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void createObservation_withBlankPostcode_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setPostcode("   ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("Postcode is required", exception.getMessage());
    }

    @Test
    void createObservation_withEmptyPostcode_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setPostcode("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("Postcode is required", exception.getMessage());
    }

    @Test
    void createObservation_withNullCitizenId_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setCitizenId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("Citizen ID is required", exception.getMessage());
    }

    @Test
    void createObservation_withBlankCitizenId_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setCitizenId("   ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("Citizen ID is required", exception.getMessage());
    }

    @Test
    void createObservation_withEmptyCitizenId_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setCitizenId("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("Citizen ID is required", exception.getMessage());
    }

    @Test
    void createObservation_withNoMeasurementsAndNoObservations_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setMeasurements(null);
        observation.setObservations(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("At least one measurement or observation is required", exception.getMessage());
    }

    @Test
    void createObservation_withNoMeasurementsAndEmptyObservations_throwsException() {
        WaterObservation observation = createValidObservation();
        observation.setMeasurements(null);
        observation.setObservations(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createObservation(observation);
        });

        assertEquals("At least one measurement or observation is required", exception.getMessage());
    }

    @Test
    void createObservation_withOnlyMeasurements_valid() {
        WaterObservation observation = createValidObservation();
        observation.setObservations(null); // Only measurements

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        verify(repository).save(observation);
    }

    @Test
    void createObservation_withOnlyObservations_valid() {
        WaterObservation observation = createValidObservation();
        observation.setMeasurements(null); // Only observations

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        verify(repository).save(observation);
    }

    @Test
    void createObservation_withPartialMeasurements_valid() {
        WaterObservation observation = createValidObservation();
        MeasurementSet measurements = new MeasurementSet();
        measurements.setPh(7.0);
        // Other measurements are null
        observation.setMeasurements(measurements);

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        verify(repository).save(observation);
    }

    @Test
    void createObservation_withAllMeasurements_valid() {
        WaterObservation observation = createValidObservation();
        MeasurementSet measurements = createFullMeasurements();
        observation.setMeasurements(measurements);

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        verify(repository).save(observation);
    }

    @Test
    void createObservation_withMultipleObservations_valid() {
        WaterObservation observation = createValidObservation();
        observation.setObservations(List.of("Clear", "Cloudy", "Murky"));

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        verify(repository).save(observation);
    }

    @Test
    void createObservation_withImageDataWithinLimit_preservesAll() {
        WaterObservation observation = createValidObservation();
        observation.setImageData(List.of("img1", "img2", "img3"));

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        assertEquals(3, result.getImageData().size());
    }

    @Test
    void createObservation_withImageDataExceedingLimit_truncatesToThree() {
        WaterObservation observation = createValidObservation();
        observation.setImageData(List.of("img1", "img2", "img3", "img4", "img5"));

        ArgumentCaptor<WaterObservation> captor = ArgumentCaptor.forClass(WaterObservation.class);
        when(repository.save(captor.capture())).thenReturn(observation);

        service.createObservation(observation);

        WaterObservation saved = captor.getValue();
        assertEquals(3, saved.getImageData().size());
        assertEquals("img1", saved.getImageData().get(0));
        assertEquals("img2", saved.getImageData().get(1));
        assertEquals("img3", saved.getImageData().get(2));
    }

    @Test
    void createObservation_withExactlyThreeImages_preservesAll() {
        WaterObservation observation = createValidObservation();
        observation.setImageData(List.of("img1", "img2", "img3"));

        ArgumentCaptor<WaterObservation> captor = ArgumentCaptor.forClass(WaterObservation.class);
        when(repository.save(captor.capture())).thenReturn(observation);

        service.createObservation(observation);

        WaterObservation saved = captor.getValue();
        assertEquals(3, saved.getImageData().size());
    }

    @Test
    void createObservation_withNullImageData_handlesGracefully() {
        WaterObservation observation = createValidObservation();
        observation.setImageData(null);

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        assertNull(result.getImageData());
    }

    @Test
    void createObservation_withEmptyImageData_handlesGracefully() {
        WaterObservation observation = createValidObservation();
        observation.setImageData(Collections.emptyList());

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
        assertTrue(result.getImageData().isEmpty());
    }

    @Test
    void listAll_returnsAllObservations() {
        List<WaterObservation> observations = List.of(
                createValidObservation(),
                createValidObservation()
        );
        when(repository.findAll()).thenReturn(observations);

        List<WaterObservation> result = service.listAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void listAll_withEmptyRepository_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<WaterObservation> result = service.listAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void listByCitizen_withValidCitizenId_returnsObservations() {
        String citizenId = "citizen-001";
        List<WaterObservation> observations = List.of(createValidObservation());
        when(repository.findByCitizenId(citizenId)).thenReturn(observations);

        List<WaterObservation> result = service.listByCitizen(citizenId);

        assertEquals(1, result.size());
        verify(repository).findByCitizenId(citizenId);
    }

    @Test
    void listByCitizen_withNonExistentCitizenId_returnsEmptyList() {
        String citizenId = "citizen-999";
        when(repository.findByCitizenId(citizenId)).thenReturn(Collections.emptyList());

        List<WaterObservation> result = service.listByCitizen(citizenId);

        assertTrue(result.isEmpty());
    }

    @Test
    void listByCitizen_withNullCitizenId_handlesGracefully() {
        when(repository.findByCitizenId(null)).thenReturn(Collections.emptyList());

        List<WaterObservation> result = service.listByCitizen(null);

        assertTrue(result.isEmpty());
        verify(repository).findByCitizenId(null);
    }

    @Test
    void createObservation_withMeasurementSetAllNullFields_valid() {
        WaterObservation observation = createValidObservation();
        MeasurementSet measurements = new MeasurementSet();
        // All fields are null, but we have observations
        observation.setMeasurements(measurements);

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
    }

    @Test
    void createObservation_withSingleMeasurementField_valid() {
        WaterObservation observation = createValidObservation();
        MeasurementSet measurements = new MeasurementSet();
        measurements.setTemperatureCelsius(20.0);
        observation.setMeasurements(measurements);

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
    }

    @Test
    void createObservation_withSingleObservation_valid() {
        WaterObservation observation = createValidObservation();
        observation.setObservations(List.of("Clear"));
        observation.setMeasurements(null);

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
    }

    @Test
    void createObservation_withVeryLongPostcode_valid() {
        WaterObservation observation = createValidObservation();
        observation.setPostcode("A".repeat(100));

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
    }

    @Test
    void createObservation_withVeryLongCitizenId_valid() {
        WaterObservation observation = createValidObservation();
        observation.setCitizenId("A".repeat(200));

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
    }

    @Test
    void createObservation_withSpecialCharactersInPostcode_valid() {
        WaterObservation observation = createValidObservation();
        observation.setPostcode("NE1-1AA/Test");

        when(repository.save(any(WaterObservation.class))).thenReturn(observation);

        WaterObservation result = service.createObservation(observation);

        assertNotNull(result);
    }

    // Helper methods
    private WaterObservation createValidObservation() {
        WaterObservation observation = new WaterObservation();
        observation.setCitizenId("citizen-001");
        observation.setPostcode("NE1 1AA");
        MeasurementSet measurements = new MeasurementSet();
        measurements.setPh(7.0);
        observation.setMeasurements(measurements);
        observation.setObservations(List.of("Clear"));
        return observation;
    }

    private MeasurementSet createFullMeasurements() {
        MeasurementSet measurements = new MeasurementSet();
        measurements.setTemperatureCelsius(20.0);
        measurements.setPh(7.0);
        measurements.setAlkalinityMgPerL(100.0);
        measurements.setTurbidityNtu(5.0);
        return measurements;
    }
}

