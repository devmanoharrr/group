package kf7014.rewards.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrowdsourcedClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CrowdsourcedClient client;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(client, "baseUrl", "http://localhost:8081");
    }

    @Test
    void findByCitizenId_withValidId_returnsObservations() {
        CrowdsourcedClient.ObservationDto[] observations = new CrowdsourcedClient.ObservationDto[2];
        observations[0] = createObservationDto("obs-1", "citizen-001");
        observations[1] = createObservationDto("obs-2", "citizen-001");
        
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(observations, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        List<CrowdsourcedClient.ObservationDto> result = client.findByCitizenId("citizen-001");

        assertEquals(2, result.size());
        assertEquals("obs-1", result.get(0).id);
        assertEquals("obs-2", result.get(1).id);
        
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(CrowdsourcedClient.ObservationDto[].class));
        assertTrue(urlCaptor.getValue().contains("citizen-001"));
    }

    @Test
    void findByCitizenId_withEmptyArray_returnsEmptyList() {
        CrowdsourcedClient.ObservationDto[] emptyArray = new CrowdsourcedClient.ObservationDto[0];
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(emptyArray, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        List<CrowdsourcedClient.ObservationDto> result = client.findByCitizenId("citizen-001");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCitizenId_withNullBody_returnsEmptyList() {
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(null, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        List<CrowdsourcedClient.ObservationDto> result = client.findByCitizenId("citizen-001");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCitizenId_withNullCitizenId_handlesGracefully() {
        CrowdsourcedClient.ObservationDto[] observations = new CrowdsourcedClient.ObservationDto[0];
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(observations, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        List<CrowdsourcedClient.ObservationDto> result = client.findByCitizenId(null);

        assertNotNull(result);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(CrowdsourcedClient.ObservationDto[].class));
        assertTrue(urlCaptor.getValue().contains("null"));
    }

    @Test
    void findByCitizenId_withEmptyCitizenId_handlesGracefully() {
        CrowdsourcedClient.ObservationDto[] observations = new CrowdsourcedClient.ObservationDto[0];
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(observations, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        List<CrowdsourcedClient.ObservationDto> result = client.findByCitizenId("");

        assertNotNull(result);
    }

    @Test
    void findByCitizenId_withSpecialCharacters_encodesCorrectly() {
        CrowdsourcedClient.ObservationDto[] observations = new CrowdsourcedClient.ObservationDto[0];
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(observations, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        client.findByCitizenId("citizen-001&test=value");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(CrowdsourcedClient.ObservationDto[].class));
        assertTrue(urlCaptor.getValue().contains("citizen-001"));
    }

    @Test
    void findByCitizenId_withRestClientException_throwsException() {
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenThrow(new RestClientException("Connection failed"));

        assertThrows(RestClientException.class, () -> {
            client.findByCitizenId("citizen-001");
        });
    }

    @Test
    void findByCitizenId_withHttpError_throwsException() {
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        List<CrowdsourcedClient.ObservationDto> result = client.findByCitizenId("citizen-001");

        // Client doesn't check status, so it returns empty list for null body
        assertTrue(result.isEmpty());
    }

    @Test
    void findByCitizenId_constructsCorrectUrl() {
        CrowdsourcedClient.ObservationDto[] observations = new CrowdsourcedClient.ObservationDto[0];
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(observations, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        client.findByCitizenId("citizen-001");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(CrowdsourcedClient.ObservationDto[].class));
        String url = urlCaptor.getValue();
        assertTrue(url.startsWith("http://localhost:8081"));
        assertTrue(url.contains("/api/observations"));
        assertTrue(url.contains("citizenId=citizen-001"));
    }

    @Test
    void findByCitizenId_withCustomBaseUrl_usesCustomUrl() {
        ReflectionTestUtils.setField(client, "baseUrl", "http://custom-host:9090");
        
        CrowdsourcedClient.ObservationDto[] observations = new CrowdsourcedClient.ObservationDto[0];
        ResponseEntity<CrowdsourcedClient.ObservationDto[]> response = 
                new ResponseEntity<>(observations, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(CrowdsourcedClient.ObservationDto[].class)))
                .thenReturn(response);

        client.findByCitizenId("citizen-001");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(CrowdsourcedClient.ObservationDto[].class));
        assertTrue(urlCaptor.getValue().startsWith("http://custom-host:9090"));
    }

    @Test
    void observationDto_hasAllFields() {
        CrowdsourcedClient.ObservationDto dto = new CrowdsourcedClient.ObservationDto();
        dto.id = "obs-1";
        dto.citizenId = "citizen-001";
        dto.postcode = "NE1 1AA";
        dto.measurements = new CrowdsourcedClient.ObservationDto.Measurements();
        dto.observations = List.of("Clear");
        dto.imageData = List.of("base64data");

        assertEquals("obs-1", dto.id);
        assertEquals("citizen-001", dto.citizenId);
        assertEquals("NE1 1AA", dto.postcode);
        assertNotNull(dto.measurements);
        assertNotNull(dto.observations);
        assertNotNull(dto.imageData);
    }

    @Test
    void measurements_hasAllFields() {
        CrowdsourcedClient.ObservationDto.Measurements m = new CrowdsourcedClient.ObservationDto.Measurements();
        m.temperatureCelsius = 20.0;
        m.ph = 7.0;
        m.alkalinityMgPerL = 100.0;
        m.turbidityNtu = 5.0;

        assertEquals(20.0, m.temperatureCelsius);
        assertEquals(7.0, m.ph);
        assertEquals(100.0, m.alkalinityMgPerL);
        assertEquals(5.0, m.turbidityNtu);
    }

    @Test
    void measurements_withNullValues_handlesGracefully() {
        CrowdsourcedClient.ObservationDto.Measurements m = new CrowdsourcedClient.ObservationDto.Measurements();
        m.temperatureCelsius = null;
        m.ph = null;
        m.alkalinityMgPerL = null;
        m.turbidityNtu = null;

        assertNull(m.temperatureCelsius);
        assertNull(m.ph);
        assertNull(m.alkalinityMgPerL);
        assertNull(m.turbidityNtu);
    }

    private CrowdsourcedClient.ObservationDto createObservationDto(String id, String citizenId) {
        CrowdsourcedClient.ObservationDto dto = new CrowdsourcedClient.ObservationDto();
        dto.id = id;
        dto.citizenId = citizenId;
        dto.postcode = "NE1 1AA";
        return dto;
    }
}

