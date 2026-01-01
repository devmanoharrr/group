package com.waterquality.gateway.adapter;

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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * API Adapter Controller for Authority-B
 * 
 * This controller implements the standardized API contract endpoints
 * required by the frontend dashboard. It adapts internal service responses
 * to match the contract format.
 */
@RestController
@RequestMapping("/api")
public class AdapterController {

    private final WebClient dataServiceClient;
    private final WebClient rewardsServiceClient;
    private final ObjectMapper objectMapper;

    public AdapterController(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.dataServiceClient = webClientBuilder.baseUrl("http://localhost:8091").build();
        this.rewardsServiceClient = webClientBuilder.baseUrl("http://localhost:8092").build();
        this.objectMapper = objectMapper;
    }

    /**
     * GET /api/observations/count
     * Returns the total count of observations
     */
    @GetMapping("/observations/count")
    public ResponseEntity<?> getObservationCount(
            @RequestParam(name = "authority", required = false) String authority) {
        try {
            String statsJson = dataServiceClient.get()
                    .uri("/api/data/stats")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode statsNode = objectMapper.readTree(statsJson);
            long total = statsNode.has("total") ? statsNode.get("total").asLong() : 0;

            Map<String, Object> response = new HashMap<>();
            response.put("count", total);
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException.ServiceUnavailable | 
                 WebClientResponseException.GatewayTimeout e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                            "Service Unavailable",
                            "Data service is currently unavailable",
                            "/api/observations/count"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal Server Error",
                            "Failed to retrieve observation count: " + e.getMessage(),
                            "/api/observations/count"));
        }
    }

    /**
     * GET /api/observations/recent?limit=5
     * Returns recent observations ordered by creation time (newest first)
     */
    @GetMapping("/observations/recent")
    public ResponseEntity<?> getRecentObservations(
            @RequestParam(name = "limit", required = false, defaultValue = "5") int limit,
            @RequestParam(name = "authority", required = false) String authority) {
        
        // Validate limit parameter
        if (limit < 1 || limit > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            "Invalid limit parameter. Must be between 1 and 50.",
                            "/api/observations/recent"));
        }

        try {
            String observationsJson = dataServiceClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/data/observations")
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode observationsArray = objectMapper.readTree(observationsJson);
            List<Map<String, Object>> response = new ArrayList<>();

            if (observationsArray.isArray()) {
                for (JsonNode observation : observationsArray) {
                    Map<String, Object> transformed = transformObservation(observation);
                    response.add(transformed);
                }
            }

            // Sort by createdAt descending (newest first) - data service should already do this
            response.sort((a, b) -> {
                String dateA = (String) a.get("createdAt");
                String dateB = (String) b.get("createdAt");
                if (dateA == null || dateB == null) return 0;
                return dateB.compareTo(dateA); // Descending
            });

            return ResponseEntity.ok(response);
        } catch (WebClientResponseException.ServiceUnavailable | 
                 WebClientResponseException.GatewayTimeout e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                            "Service Unavailable",
                            "Data service is currently unavailable",
                            "/api/observations/recent"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal Server Error",
                            "Failed to retrieve recent observations: " + e.getMessage(),
                            "/api/observations/recent"));
        }
    }

    /**
     * GET /api/rewards/leaderboard?limit=3
     * Returns top contributors ranked by points
     */
    @GetMapping("/rewards/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(name = "limit", required = false, defaultValue = "3") int limit,
            @RequestParam(name = "authority", required = false) String authority) {
        
        // Validate limit parameter
        if (limit < 1 || limit > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            "Invalid limit parameter. Must be between 1 and 50.",
                            "/api/rewards/leaderboard"));
        }

        try {
            String leaderboardJson = rewardsServiceClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/rewards/leaderboard")
                            .queryParam("top", limit)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode leaderboardNode = objectMapper.readTree(leaderboardJson);
            List<Map<String, Object>> response = new ArrayList<>();

            // Extract leaderboard array
            JsonNode leaderboardArray = leaderboardNode.has("leaderboard") 
                    ? leaderboardNode.get("leaderboard") 
                    : leaderboardNode;

            if (leaderboardArray.isArray()) {
                int rank = 1;
                for (JsonNode entry : leaderboardArray) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("contributorId", entry.has("citizenId") 
                            ? entry.get("citizenId").asText() 
                            : entry.has("contributorId") ? entry.get("contributorId").asText() : "");
                    transformed.put("points", entry.has("points") ? entry.get("points").asInt() : 0);
                    transformed.put("rank", rank++);
                    response.add(transformed);
                }
            }

            return ResponseEntity.ok(response);
        } catch (WebClientResponseException.ServiceUnavailable | 
                 WebClientResponseException.GatewayTimeout e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                            "Service Unavailable",
                            "Rewards service is currently unavailable",
                            "/api/rewards/leaderboard"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal Server Error",
                            "Failed to retrieve leaderboard: " + e.getMessage(),
                            "/api/rewards/leaderboard"));
        }
    }

    /**
     * Transforms an observation from Authority-B format to contract format
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
        
        // Observation text - check for observations array or observation field
        if (observation.has("observations") && observation.get("observations").isArray()) {
            List<String> obsList = StreamSupport.stream(
                    observation.get("observations").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
            result.put("observation", String.join(", ", obsList));
        } else if (observation.has("observation")) {
            result.put("observation", observation.get("observation").asText());
        }
        
        // CreatedAt - check for various timestamp fields
        if (observation.has("submittedAt")) {
            result.put("createdAt", observation.get("submittedAt").asText());
        } else if (observation.has("createdAt")) {
            result.put("createdAt", observation.get("createdAt").asText());
        } else if (observation.has("timestamp")) {
            result.put("createdAt", observation.get("timestamp").asText());
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

