package com.citizenscience.gateway.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * API Adapter Controller for Authority-E
 * 
 * This controller implements the standardized API contract endpoints
 * required by the frontend dashboard. It adapts internal service responses
 * to match the contract format.
 * 
 * Note: This gateway uses Spring Cloud Gateway (reactive), so all methods
 * must return Mono<ResponseEntity<?>> instead of ResponseEntity<?>.
 */
@RestController
@RequestMapping("/api")
public class AdapterController {

    private final WebClient dataServiceClient;
    private final WebClient rewardsServiceClient;
    private final ObjectMapper objectMapper;

    public AdapterController(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.dataServiceClient = webClientBuilder.baseUrl("http://localhost:8121").build();
        this.rewardsServiceClient = webClientBuilder.baseUrl("http://localhost:8122").build();
        this.objectMapper = objectMapper;
    }

    /**
     * GET /api/observations/count
     * Returns the total count of observations
     */
    @GetMapping("/observations/count")
    public Mono<ResponseEntity<?>> getObservationCount(
            @RequestParam(name = "authority", required = false) String authority) {
        return dataServiceClient.get()
                .uri("/observations")
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.boundedElastic())
                .map(observationsJson -> {
                    try {
                        JsonNode observationsArray = objectMapper.readTree(observationsJson);
                        long count = observationsArray.isArray() ? observationsArray.size() : 0;
                        Map<String, Object> response = new HashMap<>();
                        response.put("count", count);
                        return ResponseEntity.<Object>ok(response);
                    } catch (Exception e) {
                        return ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                        "Internal Server Error",
                                        "Failed to retrieve observation count: " + e.getMessage(),
                                        "/api/observations/count"));
                    }
                })
                .onErrorResume(WebClientResponseException.ServiceUnavailable.class, e ->
                        Mono.just(ResponseEntity.<Object>status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                        "Service Unavailable",
                                        "Data service is currently unavailable",
                                        "/api/observations/count"))))
                .onErrorResume(WebClientResponseException.GatewayTimeout.class, e ->
                        Mono.just(ResponseEntity.<Object>status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                        "Service Unavailable",
                                        "Data service is currently unavailable",
                                        "/api/observations/count"))))
                .onErrorResume(e -> Mono.just(ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "Failed to retrieve observation count: " + e.getMessage(),
                                "/api/observations/count"))));
    }

    /**
     * GET /api/observations/recent?limit=5
     * Returns recent observations ordered by creation time (newest first)
     */
    @GetMapping("/observations/recent")
    public Mono<ResponseEntity<?>> getRecentObservations(
            @RequestParam(name = "limit", required = false, defaultValue = "5") int limit,
            @RequestParam(name = "authority", required = false) String authority) {
        
        // Validate limit parameter
        if (limit < 1 || limit > 50) {
            return Mono.just(ResponseEntity.<Object>status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            "Invalid limit parameter. Must be between 1 and 50.",
                            "/api/observations/recent")));
        }

        return dataServiceClient.get()
                .uri("/observations")
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.boundedElastic())
                .map(observationsJson -> {
                    try {
                        JsonNode observationsArray = objectMapper.readTree(observationsJson);
                        List<Map<String, Object>> response = new ArrayList<>();

                        if (observationsArray.isArray()) {
                            List<JsonNode> observations = new ArrayList<>();
                            for (JsonNode observation : observationsArray) {
                                observations.add(observation);
                            }
                            
                            // Sort by submittedAt or timestamp descending (newest first)
                            observations.sort((a, b) -> {
                                String dateA = getTimestamp(a);
                                String dateB = getTimestamp(b);
                                return dateB.compareTo(dateA); // Descending
                            });
                            
                            // Limit and transform
                            for (int i = 0; i < Math.min(limit, observations.size()); i++) {
                                Map<String, Object> transformed = transformObservation(observations.get(i));
                                response.add(transformed);
                            }
                        }

                        return ResponseEntity.<Object>ok(response);
                    } catch (Exception e) {
                        return ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                        "Internal Server Error",
                                        "Failed to retrieve recent observations: " + e.getMessage(),
                                        "/api/observations/recent"));
                    }
                })
                .onErrorResume(WebClientResponseException.ServiceUnavailable.class, e ->
                        Mono.just(ResponseEntity.<Object>status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                        "Service Unavailable",
                                        "Data service is currently unavailable",
                                        "/api/observations/recent"))))
                .onErrorResume(WebClientResponseException.GatewayTimeout.class, e ->
                        Mono.just(ResponseEntity.<Object>status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                        "Service Unavailable",
                                        "Data service is currently unavailable",
                                        "/api/observations/recent"))))
                .onErrorResume(e -> Mono.just(ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "Failed to retrieve recent observations: " + e.getMessage(),
                                "/api/observations/recent"))));
    }

    /**
     * GET /api/rewards/leaderboard?limit=3
     * Returns top contributors ranked by points
     */
    @GetMapping("/rewards/leaderboard")
    public Mono<ResponseEntity<?>> getLeaderboard(
            @RequestParam(name = "limit", required = false, defaultValue = "3") int limit,
            @RequestParam(name = "authority", required = false) String authority) {
        
        // Validate limit parameter
        if (limit < 1 || limit > 50) {
            return Mono.just(ResponseEntity.<Object>status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            "Invalid limit parameter. Must be between 1 and 50.",
                            "/api/rewards/leaderboard")));
        }

        // Get all cached reward summaries
        return rewardsServiceClient.get()
                .uri("/rewards")
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.boundedElastic())
                .map(rewardsJson -> {
                    try {
                        JsonNode rewardsArray = objectMapper.readTree(rewardsJson);
                        List<Map<String, Object>> response = new ArrayList<>();

                        if (rewardsArray.isArray()) {
                            List<Map<String, Object>> leaderboard = new ArrayList<>();
                            
                            for (JsonNode reward : rewardsArray) {
                                Map<String, Object> entry = new HashMap<>();
                                entry.put("contributorId", reward.has("citizenId") 
                                        ? reward.get("citizenId").asText() 
                                        : "");
                                entry.put("points", reward.has("totalPoints") 
                                        ? reward.get("totalPoints").asInt() 
                                        : 0);
                                leaderboard.add(entry);
                            }
                            
                            // Sort by points descending
                            leaderboard.sort((a, b) -> {
                                Integer pointsA = (Integer) a.get("points");
                                Integer pointsB = (Integer) b.get("points");
                                return pointsB.compareTo(pointsA); // Descending
                            });
                            
                            // Limit and add rank
                            for (int i = 0; i < Math.min(limit, leaderboard.size()); i++) {
                                Map<String, Object> entry = leaderboard.get(i);
                                entry.put("rank", i + 1);
                                response.add(entry);
                            }
                        }

                        return ResponseEntity.<Object>ok(response);
                    } catch (Exception e) {
                        return ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                        "Internal Server Error",
                                        "Failed to retrieve leaderboard: " + e.getMessage(),
                                        "/api/rewards/leaderboard"));
                    }
                })
                .onErrorResume(WebClientResponseException.ServiceUnavailable.class, e ->
                        Mono.just(ResponseEntity.<Object>status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                        "Service Unavailable",
                                        "Rewards service is currently unavailable",
                                        "/api/rewards/leaderboard"))))
                .onErrorResume(WebClientResponseException.GatewayTimeout.class, e ->
                        Mono.just(ResponseEntity.<Object>status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                        "Service Unavailable",
                                        "Rewards service is currently unavailable",
                                        "/api/rewards/leaderboard"))))
                .onErrorResume(e -> Mono.just(ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "Failed to retrieve leaderboard: " + e.getMessage(),
                                "/api/rewards/leaderboard"))));
    }

    /**
     * Gets timestamp from observation node
     */
    private String getTimestamp(JsonNode observation) {
        if (observation.has("submittedAt")) {
            return observation.get("submittedAt").asText();
        } else if (observation.has("createdAt")) {
            return observation.get("createdAt").asText();
        } else if (observation.has("timestamp")) {
            return observation.get("timestamp").asText();
        }
        return "";
    }

    /**
     * Transforms an observation from Authority-E format to contract format
     */
    private Map<String, Object> transformObservation(JsonNode observation) {
        Map<String, Object> result = new HashMap<>();
        
        // Postcode
        if (observation.has("postcode")) {
            result.put("postcode", observation.get("postcode").asText());
        }
        
        // Measurements object
        Map<String, Object> measurements = new HashMap<>();
        if (observation.has("measurements")) {
            JsonNode measurementsNode = observation.get("measurements");
            if (measurementsNode.has("temperature")) {
                measurements.put("temperature", measurementsNode.get("temperature").asDouble());
            }
            if (measurementsNode.has("ph")) {
                measurements.put("pH", measurementsNode.get("ph").asDouble());
            }
            if (measurementsNode.has("alkalinity")) {
                measurements.put("alkalinity", measurementsNode.get("alkalinity").asDouble());
            }
            if (measurementsNode.has("turbidity")) {
                measurements.put("turbidity", measurementsNode.get("turbidity").asDouble());
            }
        }
        result.put("measurements", measurements);
        
        // Observation text - observations is an array
        if (observation.has("observations") && observation.get("observations").isArray()) {
            List<String> obsList = StreamSupport.stream(
                    observation.get("observations").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
            result.put("observation", String.join(", ", obsList));
        } else if (observation.has("observation")) {
            result.put("observation", observation.get("observation").asText());
        }
        
        // CreatedAt
        String timestamp = getTimestamp(observation);
        if (!timestamp.isEmpty()) {
            result.put("createdAt", timestamp);
        }
        
        // Contributor ID
        if (observation.has("citizenId")) {
            result.put("contributorId", observation.get("citizenId").asText());
        } else if (observation.has("contributorId")) {
            result.put("contributorId", observation.get("contributorId").asText());
        }
        
        return result;
    }

    /**
     * Creates a standardized error response
     */
    private Map<String, Object> createErrorResponse(int status, String error, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT));
        errorResponse.put("status", status);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}
