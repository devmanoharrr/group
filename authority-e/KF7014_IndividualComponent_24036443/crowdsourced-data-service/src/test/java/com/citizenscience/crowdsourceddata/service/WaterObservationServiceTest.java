package com.citizenscience.crowdsourceddata.service;

import com.citizenscience.crowdsourceddata.dto.WaterObservationRequest;
import com.citizenscience.crowdsourceddata.dto.WaterObservationRequest.MeasurementsPayload;
import com.citizenscience.crowdsourceddata.dto.WaterObservationResponse;
import com.citizenscience.crowdsourceddata.exception.CitizenNotFoundException;
import com.citizenscience.crowdsourceddata.model.CitizenProfile;
import com.citizenscience.crowdsourceddata.model.ObservationCondition;
import com.citizenscience.crowdsourceddata.model.WaterObservation;
import com.citizenscience.crowdsourceddata.repository.WaterObservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterObservationServiceTest {

    @Mock
    private WaterObservationRepository repository;

    @Mock
    private CitizenService citizenService;

    @InjectMocks
    private WaterObservationService service;

    private WaterObservationRequest baseRequest;

    @BeforeEach
    void setUp() {
        baseRequest = new WaterObservationRequest();
        baseRequest.setPostcode("NE1 1AA");
        baseRequest.setTemperature(12.5);
        baseRequest.setObservations(List.of("Clear"));
    }

    @Test
    void saveObservationPersistsRecord() {
        when(citizenService.exists("citizen-123")).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WaterObservationResponse response = service.saveObservation("citizen-123", baseRequest);

        assertThat(response.getCitizenId()).isEqualTo("citizen-123");
        ArgumentCaptor<WaterObservation> captor = ArgumentCaptor.forClass(WaterObservation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSubmissionTimestamp()).isNotNull();
    }

    @Test
    void saveObservationInitialisesMutableConditionsSet() {
        when(citizenService.exists("citizen-123")).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveObservation("citizen-123", baseRequest);

        ArgumentCaptor<WaterObservation> captor = ArgumentCaptor.forClass(WaterObservation.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getConditions()).contains(ObservationCondition.CLEAR);
        assertThatCode(() -> captor.getValue().getConditions().add(ObservationCondition.CLOUDY))
                .doesNotThrowAnyException();
    }

    @Test
    void saveObservationThrowsWhenMissingObservations() {
        baseRequest.setObservations(null);

        assertThatThrownBy(() -> service.saveObservation("citizen-123", baseRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one observation must be provided");
    }

    @Test
    void saveObservationMarksRecordCompleteWhenAllDataProvided() {
        baseRequest.setPh(7.0);
        baseRequest.setAlkalinity(20.0);
        baseRequest.setTurbidity(2.5);
        baseRequest.setObservations(List.of(ObservationCondition.CLEAR.name()));
        baseRequest.setImages(List.of("aW1hZ2U="));

        when(citizenService.exists("citizen-123")).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WaterObservationResponse response = service.saveObservation("citizen-123", baseRequest);

        assertThat(response.isComplete()).isTrue();
    }

    @Test
    void saveObservationAcceptsMeasurementsPayload() {
        MeasurementsPayload payload = new MeasurementsPayload();
        payload.setTemperature(9.5);
        payload.setPh(6.8);

        WaterObservationRequest request = new WaterObservationRequest();
        request.setPostcode("NE2 2BB");
        request.setMeasurements(payload);
        request.setObservations(List.of("Clear"));

        when(citizenService.exists("citizen-321")).thenReturn(true);

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WaterObservationResponse response = service.saveObservation("citizen-321", request);

        assertThat(response.getTemperature()).isEqualTo(9.5);
        assertThat(response.getPh()).isEqualTo(6.8);
    }

    @Test
    void getObservationThrowsWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getObservation("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Observation not found");
    }

    @Test
    void saveObservationFailsWhenCitizenMissing() {
        when(citizenService.exists("citizen-999")).thenReturn(false);

        assertThatThrownBy(() -> service.saveObservation("citizen-999", baseRequest))
                .isInstanceOf(CitizenNotFoundException.class)
                .hasMessageContaining("citizen-999");
    }

    @Test
    void getObservationsByCitizenFailsWhenCitizenMissing() {
        when(citizenService.exists("citizen-999")).thenReturn(false);

        assertThatThrownBy(() -> service.getObservationsByCitizenId("citizen-999"))
                .isInstanceOf(CitizenNotFoundException.class)
                .hasMessageContaining("citizen-999");
    }

    @Test
    void saveObservationRejectsBlankCitizenId() {
        assertThatThrownBy(() -> service.saveObservation("   ", baseRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Citizen ID is required");
    }

    @Test
    void getObservationsByCitizenRejectsBlankCitizenId() {
        assertThatThrownBy(() -> service.getObservationsByCitizenId("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Citizen ID is required");
    }

    @Test
    void saveObservationRejectsBlankPostcode() {
        baseRequest.setPostcode("   ");

        assertThatThrownBy(() -> service.saveObservation("citizen-123", baseRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Postcode is required");
    }

    @Test
    void saveObservationRejectsTooManyImages() {
        baseRequest.setImages(List.of("a", "b", "c", "d"));

        assertThatThrownBy(() -> service.saveObservation("citizen-123", baseRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A maximum of three images");
    }

    @Test
    void saveObservationForNewCitizenRegistersProfile() {
        CitizenProfile profile = CitizenProfile.create(1);
        when(citizenService.registerCitizen()).thenReturn(profile);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WaterObservationResponse response = service.saveObservationForNewCitizen(baseRequest);

        assertThat(response.getCitizenId()).isEqualTo(profile.getId());
        verify(citizenService).registerCitizen();
    }

    @Test
    void getObservationsByCitizenSortsByTimestamp() {
        when(citizenService.exists("citizen-123")).thenReturn(true);

        WaterObservation first = new WaterObservation();
        first.setSubmissionTimestamp(LocalDateTime.now().minusHours(1));

        WaterObservation second = new WaterObservation();
        second.setSubmissionTimestamp(LocalDateTime.now());

        when(repository.findByCitizenId("citizen-123")).thenReturn(List.of(second, first));

        List<WaterObservationResponse> responses = service.getObservationsByCitizenId("citizen-123");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getSubmissionTimestamp())
                .isBefore(responses.get(1).getSubmissionTimestamp());
    }
}
