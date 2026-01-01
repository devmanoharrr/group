package kf7014.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DownstreamClients {

    private final WebClient crowdsourcedClient;
    private final WebClient rewardsClient;

    public DownstreamClients(
            @Value("${services.crowdsourced.baseUrl:http://localhost:8081}") String crowdsourcedBaseUrl,
            @Value("${services.rewards.baseUrl:http://localhost:8082}") String rewardsBaseUrl
    ) {
        this.crowdsourcedClient = WebClient.builder().baseUrl(crowdsourcedBaseUrl).build();
        this.rewardsClient = WebClient.builder().baseUrl(rewardsBaseUrl).build();
    }

    public Mono<String> createObservation(String jsonBody) {
        return crowdsourcedClient.post()
                .uri("/api/observations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonBody))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> listObservations(String citizenId) {
        String uri = (citizenId == null || citizenId.isBlank()) ? "/api/observations" : "/api/observations?citizenId=" + citizenId;
        return crowdsourcedClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> recomputeRewards(String citizenId) {
        return rewardsClient.post()
                .uri("/api/rewards/recompute/" + citizenId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getRewards(String citizenId) {
        return rewardsClient.get()
                .uri("/api/rewards/" + citizenId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> checkCrowdsourcedHealth() {
        return crowdsourcedClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> response.contains("\"status\":\"UP\"") ? "UP" : "DOWN")
                .onErrorReturn("DOWN");
    }

    public Mono<String> checkRewardsHealth() {
        return rewardsClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> response.contains("\"status\":\"UP\"") ? "UP" : "DOWN")
                .onErrorReturn("DOWN");
    }
}


