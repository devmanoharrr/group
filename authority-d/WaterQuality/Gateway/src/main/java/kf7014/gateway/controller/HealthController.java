package kf7014.gateway.controller;

import kf7014.gateway.client.DownstreamClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final DownstreamClients clients;

    public HealthController(DownstreamClients clients) {
        this.clients = clients;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("gateway", "UP");

        return checkDownstreamServices()
                .map(downstreamHealth -> {
                    health.putAll(downstreamHealth);
                    
                    boolean allServicesUp = downstreamHealth.values().stream()
                            .allMatch(status -> "UP".equals(status));
                    
                    if (!allServicesUp) {
                        health.put("status", "DEGRADED");
                    }
                    
                    HttpStatus status = allServicesUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                    return ResponseEntity.status(status).body(health);
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "DOWN", "gateway", "UP", "error", "Health check timeout")));
    }

    private Mono<Map<String, Object>> checkDownstreamServices() {
        Map<String, Object> downstreamHealth = new HashMap<>();
        
        Mono<String> crowdsourcedHealth = clients.checkCrowdsourcedHealth()
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn("DOWN");
        
        Mono<String> rewardsHealth = clients.checkRewardsHealth()
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn("DOWN");
        
        return Mono.zip(crowdsourcedHealth, rewardsHealth)
                .map(tuple -> {
                    downstreamHealth.put("crowdsourcedService", tuple.getT1());
                    downstreamHealth.put("rewardsService", tuple.getT2());
                    return downstreamHealth;
                });
    }
}

