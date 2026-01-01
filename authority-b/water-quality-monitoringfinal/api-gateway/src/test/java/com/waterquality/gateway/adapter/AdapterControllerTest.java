package com.waterquality.gateway.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Smoke tests for Authority-B Adapter Controller
 * Tests the standardized API contract endpoints
 */
@ExtendWith(MockitoExtension.class)
class AdapterControllerTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient dataServiceClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AdapterController adapterController;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(dataServiceClient);
    }

    @Test
    void testGetObservationCount_Returns200() throws Exception {
        // Mock successful response
        String statsJson = "{\"total\": 10}";
        when(dataServiceClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(statsJson));
        when(objectMapper.readTree(statsJson)).thenReturn(new ObjectMapper().readTree(statsJson));

        ResponseEntity<?> response = adapterController.getObservationCount(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("count"));
    }

    @Test
    void testGetRecentObservations_ValidatesLimit() {
        // Test invalid limit (too high)
        ResponseEntity<?> response = adapterController.getRecentObservations(100, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void testGetLeaderboard_ValidatesLimit() {
        // Test invalid limit (too high)
        ResponseEntity<?> response = adapterController.getLeaderboard(100, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("error"));
    }
}

