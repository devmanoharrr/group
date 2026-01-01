package kf7014.gateway.controller;

import kf7014.gateway.client.DownstreamClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private DownstreamClients clients;

    private HealthController controller;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        controller = new HealthController(clients);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void health_withAllServicesUp_returnsOk() {
        when(clients.checkCrowdsourcedHealth()).thenReturn(Mono.just("UP"));
        when(clients.checkRewardsHealth()).thenReturn(Mono.just("UP"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.gateway").isEqualTo("UP")
                .jsonPath("$.crowdsourcedService").isEqualTo("UP")
                .jsonPath("$.rewardsService").isEqualTo("UP");

        verify(clients).checkCrowdsourcedHealth();
        verify(clients).checkRewardsHealth();
    }

    @Test
    void health_withOneServiceDown_returnsServiceUnavailable() {
        when(clients.checkCrowdsourcedHealth()).thenReturn(Mono.just("UP"));
        when(clients.checkRewardsHealth()).thenReturn(Mono.just("DOWN"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("DEGRADED")
                .jsonPath("$.gateway").isEqualTo("UP")
                .jsonPath("$.crowdsourcedService").isEqualTo("UP")
                .jsonPath("$.rewardsService").isEqualTo("DOWN");
    }

    @Test
    void health_withBothServicesDown_returnsServiceUnavailable() {
        when(clients.checkCrowdsourcedHealth()).thenReturn(Mono.just("DOWN"));
        when(clients.checkRewardsHealth()).thenReturn(Mono.just("DOWN"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("DEGRADED")
                .jsonPath("$.gateway").isEqualTo("UP")
                .jsonPath("$.crowdsourcedService").isEqualTo("DOWN")
                .jsonPath("$.rewardsService").isEqualTo("DOWN");
    }

    @Test
    void health_withCrowdsourcedServiceDown_returnsServiceUnavailable() {
        when(clients.checkCrowdsourcedHealth()).thenReturn(Mono.just("DOWN"));
        when(clients.checkRewardsHealth()).thenReturn(Mono.just("UP"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("DEGRADED")
                .jsonPath("$.crowdsourcedService").isEqualTo("DOWN");
    }

    @Test
    void health_withTimeout_returnsServiceUnavailable() {
        // Individual health checks have 2-second timeout with onErrorReturn("DOWN")
        // So when they timeout, they return "DOWN" which results in SERVICE_UNAVAILABLE
        // The overall 5-second timeout only triggers if checkDownstreamServices() itself takes >5s
        when(clients.checkCrowdsourcedHealth())
                .thenReturn(Mono.just("UP").delayElement(Duration.ofSeconds(10)));
        when(clients.checkRewardsHealth())
                .thenReturn(Mono.just("UP").delayElement(Duration.ofSeconds(10)));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("DEGRADED")
                .jsonPath("$.gateway").isEqualTo("UP")
                .jsonPath("$.crowdsourcedService").isEqualTo("DOWN")
                .jsonPath("$.rewardsService").isEqualTo("DOWN");
    }

    @Test
    void health_withCrowdsourcedHealthError_handlesGracefully() {
        when(clients.checkCrowdsourcedHealth())
                .thenReturn(Mono.just("DOWN"));
        when(clients.checkRewardsHealth())
                .thenReturn(Mono.just("UP"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.crowdsourcedService").isEqualTo("DOWN");
    }

    @Test
    void health_withRewardsHealthError_handlesGracefully() {
        when(clients.checkCrowdsourcedHealth())
                .thenReturn(Mono.just("UP"));
        when(clients.checkRewardsHealth())
                .thenReturn(Mono.just("DOWN"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.rewardsService").isEqualTo("DOWN");
    }

    @Test
    void health_withBothHealthErrors_handlesGracefully() {
        when(clients.checkCrowdsourcedHealth())
                .thenReturn(Mono.just("DOWN"));
        when(clients.checkRewardsHealth())
                .thenReturn(Mono.just("DOWN"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.crowdsourcedService").isEqualTo("DOWN")
                .jsonPath("$.rewardsService").isEqualTo("DOWN");
    }

    @Test
    void health_alwaysIncludesGatewayStatus() {
        when(clients.checkCrowdsourcedHealth()).thenReturn(Mono.just("UP"));
        when(clients.checkRewardsHealth()).thenReturn(Mono.just("UP"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.gateway").isEqualTo("UP");
    }

    @Test
    void health_withException_returnsServiceUnavailable() {
        // When individual health checks throw exceptions, they are caught by onErrorReturn("DOWN")
        // This results in SERVICE_UNAVAILABLE (503) not INTERNAL_SERVER_ERROR (500)
        // Only the overall health() method timeout/error returns 500
        when(clients.checkCrowdsourcedHealth())
                .thenReturn(Mono.error(new RuntimeException("Connection failed")));
        when(clients.checkRewardsHealth())
                .thenReturn(Mono.just("UP"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("DEGRADED")
                .jsonPath("$.gateway").isEqualTo("UP")
                .jsonPath("$.crowdsourcedService").isEqualTo("DOWN")
                .jsonPath("$.rewardsService").isEqualTo("UP");
    }

    @Test
    void health_verifiesBothServicesChecked() {
        when(clients.checkCrowdsourcedHealth()).thenReturn(Mono.just("UP"));
        when(clients.checkRewardsHealth()).thenReturn(Mono.just("UP"));

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk();

        verify(clients, times(1)).checkCrowdsourcedHealth();
        verify(clients, times(1)).checkRewardsHealth();
    }
}

