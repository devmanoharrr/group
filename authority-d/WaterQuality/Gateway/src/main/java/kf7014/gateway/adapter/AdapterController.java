package kf7014.gateway.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kf7014.gateway.client.DownstreamClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * API Adapter Controller for Authority-D
 * 
 * This controller implements the standardized API contract endpoints
 * required by the frontend dashboard. It adapts internal service responses
 * to match the contract format.
 */
@RestController
@RequestMapping("/api")
public class AdapterController {

    private final DownstreamClients clients;
    private final ObjectMapper objectMapper;
    private final WebClient rewardsClient;

    public AdapterController(DownstreamClients clients, ObjectMapper objectMapper) {
        this.clients = clients;
        this.objectMapper = objectMapper;
        // Create a WebClient for rewards service to get individual rewards
        this.rewardsClient = WebClient.builder()
                .baseUrl("http://localhost:8112")
                .build();
    }

    /**
     * GET /api/observations/count
     * Returns the total count of observations
     */
    @GetMapping("/observations/count")
    public Mono<ResponseEntity<?>> getObservationCount(
            @RequestParam(name = "authority", required = false) String authority) {
        return clients.listObservations(null)
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

        return clients.listObservations(null)
                .map(observationsJson -> {
                    try {
                        JsonNode observationsArray = objectMapper.readTree(observationsJson);
                        List<Map<String, Object>> response = new ArrayList<>();
                        
                        if (observationsArray.isArray()) {
                            List<JsonNode> observations = new ArrayList<>();
                            for (JsonNode observation : observationsArray) {
                                observations.add(observation);
                            }
                            
                            // Sort by submittedAt descending (newest first)
                            observations.sort((a, b) -> {
                                String dateA = a.has("submittedAt") ? a.get("submittedAt").asText() : "";
                                String dateB = b.has("submittedAt") ? b.get("submittedAt").asText() : "";
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

        // First, get all observations to extract unique citizen IDs
        return clients.listObservations(null)
                .flatMap(observationsJson -> {
                    try {
                        JsonNode observationsArray = objectMapper.readTree(observationsJson);
                        Set<String> citizenIds = new HashSet<>();
                        
                        if (observationsArray.isArray()) {
                            for (JsonNode observation : observationsArray) {
                                if (observation.has("citizenId")) {
                                    citizenIds.add(observation.get("citizenId").asText());
                                }
                            }
                        }
                        
                        // Now fetch rewards for each citizen
                        List<Mono<Map<String, Object>>> rewardMonos = citizenIds.stream()
                                .map(citizenId -> rewardsClient.get()
                                        .uri("/api/rewards/" + citizenId)
                                        .retrieve()
                                        .bodyToMono(String.class)
                                        .map(rewardJson -> {
                                            try {
                                                JsonNode rewardNode = objectMapper.readTree(rewardJson);
                                                Map<String, Object> entry = new HashMap<>();
                                                entry.put("contributorId", rewardNode.has("citizenId") 
                                                        ? rewardNode.get("citizenId").asText() 
                                                        : citizenId);
                                                entry.put("points", rewardNode.has("totalPoints") 
                                                        ? rewardNode.get("totalPoints").asInt() 
                                                        : 0);
                                                return entry;
                                            } catch (Exception e) {
                                                Map<String, Object> entry = new HashMap<>();
                                                entry.put("contributorId", citizenId);
                                                entry.put("points", 0);
                                                return entry;
                                            }
                                        })
                                        .onErrorReturn(createDefaultEntry(citizenId)))
                                .collect(Collectors.toList());
                        
                        // Combine all monos and sort
                        return Mono.zip(rewardMonos, results -> {
                            List<Map<String, Object>> leaderboard = Arrays.stream(results)
                                    .map(obj -> (Map<String, Object>) obj)
                                    .sorted((a, b) -> {
                                        Integer pointsA = (Integer) a.get("points");
                                        Integer pointsB = (Integer) b.get("points");
                                        return pointsB.compareTo(pointsA); // Descending
                                    })
                                    .limit(limit)
                                    .collect(Collectors.toList());
                            
                            // Add rank numbers
                            for (int i = 0; i < leaderboard.size(); i++) {
                                leaderboard.get(i).put("rank", i + 1);
                            }
                            
                            return ResponseEntity.<Object>ok(leaderboard);
                        });
                    } catch (Exception e) {
                        return Mono.just(ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                        "Internal Server Error",
                                        "Failed to retrieve leaderboard: " + e.getMessage(),
                                        "/api/rewards/leaderboard")));
                    }
                })
                .onErrorResume(WebClientResponseException.ServiceUnavailable.class, e ->
                        Mono.just(ResponseEntity.<Object>status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                        "Service Unavailable",
                                        "Service is currently unavailable",
                                        "/api/rewards/leaderboard"))))
                .onErrorResume(e -> Mono.just(ResponseEntity.<Object>status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "Failed to retrieve leaderboard: " + e.getMessage(),
                                "/api/rewards/leaderboard"))));
    }

    /**
     * Transforms an observation from Authority-D format to contract format
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
            if (measurementsNode.has("temperatureCelsius")) {
                measurements.put("temperature", measurementsNode.get("temperatureCelsius").asDouble());
            }
            if (measurementsNode.has("ph")) {
                measurements.put("pH", measurementsNode.get("ph").asDouble());
            }
            if (measurementsNode.has("alkalinityMgPerL")) {
                measurements.put("alkalinity", measurementsNode.get("alkalinityMgPerL").asDouble());
            }
            if (measurementsNode.has("turbidityNtu")) {
                measurements.put("turbidity", measurementsNode.get("turbidityNtu").asDouble());
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
        }
        
        // CreatedAt - use submittedAt
        if (observation.has("submittedAt")) {
            String submittedAt = observation.get("submittedAt").asText();
            // Ensure ISO 8601 format
            result.put("createdAt", submittedAt);
        }
        
        // Contributor ID
        if (observation.has("citizenId")) {
            result.put("contributorId", observation.get("citizenId").asText());
        }
        
        return result;
    }

    /**
     * Creates a default entry for leaderboard when reward fetch fails
     */
    private Map<String, Object> createDefaultEntry(String citizenId) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("contributorId", citizenId);
        entry.put("points", 0);
        return entry;
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

