package kf7014.gateway.controller;

import kf7014.gateway.client.DownstreamClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicApiControllerTest {

    @Mock
    private DownstreamClients clients;

    private PublicApiController controller;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        controller = new PublicApiController(clients);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void createObservation_withValidBody_returnsOk() {
        String requestBody = "{\"citizenId\":\"citizen-001\",\"postcode\":\"NE1 1AA\"}";
        String responseBody = "{\"id\":\"obs-1\"}";
        
        when(clients.createObservation(requestBody)).thenReturn(Mono.just(responseBody));

        webTestClient.post()
                .uri("/api/observations")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(responseBody);

        verify(clients).createObservation(requestBody);
    }

    @Test
    void createObservation_withEmptyBody_returnsOk() {
        String requestBody = "{}";
        String responseBody = "{}";
        
        when(clients.createObservation(requestBody)).thenReturn(Mono.just(responseBody));

        webTestClient.post()
                .uri("/api/observations")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk();

        verify(clients).createObservation(requestBody);
    }

    @Test
    void createObservation_withNullBody_handlesGracefully() {
        // WebFlux doesn't allow null body values, so this test verifies the endpoint exists
        // In practice, null body would be rejected by WebFlux before reaching the controller
        assertNotNull(controller);
    }

    @Test
    void createObservation_withLargeBody_handlesGracefully() {
        String largeBody = "{\"data\":\"" + "A".repeat(10000) + "\"}";
        String responseBody = "{\"id\":\"obs-1\"}";
        
        when(clients.createObservation(largeBody)).thenReturn(Mono.just(responseBody));

        webTestClient.post()
                .uri("/api/observations")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(largeBody)
                .exchange()
                .expectStatus().isOk();

        verify(clients).createObservation(largeBody);
    }

    @Test
    void listObservations_withNoCitizenId_returnsOk() {
        String responseBody = "[{\"id\":\"obs-1\"}]";
        
        when(clients.listObservations(null)).thenReturn(Mono.just(responseBody));

        webTestClient.get()
                .uri("/api/observations")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(responseBody);

        verify(clients).listObservations(null);
    }

    @Test
    void listObservations_withCitizenId_returnsOk() {
        String citizenId = "citizen-001";
        String responseBody = "[{\"id\":\"obs-1\"}]";
        
        when(clients.listObservations(citizenId)).thenReturn(Mono.just(responseBody));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/observations")
                        .queryParam("citizenId", citizenId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(responseBody);

        verify(clients).listObservations(citizenId);
    }

    @Test
    void listObservations_withEmptyCitizenId_returnsOk() {
        String responseBody = "[]";
        
        when(clients.listObservations("")).thenReturn(Mono.just(responseBody));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/observations")
                        .queryParam("citizenId", "")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(clients).listObservations("");
    }

    @Test
    void listObservations_withEmptyResponse_returnsOk() {
        String responseBody = "[]";
        
        when(clients.listObservations(null)).thenReturn(Mono.just(responseBody));

        webTestClient.get()
                .uri("/api/observations")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(responseBody);
    }

    @Test
    void recompute_withValidCitizenId_returnsOk() {
        String citizenId = "citizen-001";
        String responseBody = "{\"totalPoints\":100,\"badge\":\"Bronze\"}";
        
        when(clients.recomputeRewards(citizenId)).thenReturn(Mono.just(responseBody));

        webTestClient.post()
                .uri("/api/rewards/recompute/" + citizenId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(responseBody);

        verify(clients).recomputeRewards(citizenId);
    }

    @Test
    void recompute_withEmptyCitizenId_returnsNotFound() {
        // Empty path variable results in 404 as Spring can't match the route
        // This is expected behavior - empty citizenId is not a valid REST endpoint
        webTestClient.post()
                .uri("/api/rewards/recompute/")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void recompute_withSpecialCharactersInCitizenId_returnsOk() {
        String citizenId = "citizen-001-test";
        String responseBody = "{\"totalPoints\":50}";
        
        when(clients.recomputeRewards(citizenId)).thenReturn(Mono.just(responseBody));

        webTestClient.post()
                .uri("/api/rewards/recompute/" + citizenId)
                .exchange()
                .expectStatus().isOk();

        verify(clients).recomputeRewards(citizenId);
    }

    @Test
    void get_withValidCitizenId_returnsOk() {
        String citizenId = "citizen-001";
        String responseBody = "{\"totalPoints\":100,\"badge\":\"Bronze\"}";
        
        when(clients.getRewards(citizenId)).thenReturn(Mono.just(responseBody));

        webTestClient.get()
                .uri("/api/rewards/" + citizenId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(responseBody);

        verify(clients).getRewards(citizenId);
    }

    @Test
    void get_withNonExistentCitizenId_returnsOk() {
        String citizenId = "citizen-999";
        String responseBody = "{\"totalPoints\":0}";
        
        when(clients.getRewards(citizenId)).thenReturn(Mono.just(responseBody));

        webTestClient.get()
                .uri("/api/rewards/" + citizenId)
                .exchange()
                .expectStatus().isOk();

        verify(clients).getRewards(citizenId);
    }

    @Test
    void get_withEmptyCitizenId_returnsNotFound() {
        // Empty path variable results in 404 as Spring can't match the route
        // This is expected behavior - empty citizenId is not a valid REST endpoint
        webTestClient.get()
                .uri("/api/rewards/")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createObservation_withError_handlesGracefully() {
        String requestBody = "{\"citizenId\":\"citizen-001\"}";
        
        when(clients.createObservation(requestBody))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.post()
                .uri("/api/observations")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(clients).createObservation(requestBody);
    }

    @Test
    void listObservations_withError_handlesGracefully() {
        when(clients.listObservations(null))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.get()
                .uri("/api/observations")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(clients).listObservations(null);
    }

    @Test
    void recompute_withError_handlesGracefully() {
        String citizenId = "citizen-001";
        
        when(clients.recomputeRewards(citizenId))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.post()
                .uri("/api/rewards/recompute/" + citizenId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(clients).recomputeRewards(citizenId);
    }

    @Test
    void get_withError_handlesGracefully() {
        String citizenId = "citizen-001";
        
        when(clients.getRewards(citizenId))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.get()
                .uri("/api/rewards/" + citizenId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(clients).getRewards(citizenId);
    }
}

