package com.bharath.wq.gateway.adapter;

import com.bharath.wq.gateway.config.UpstreamsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * API Adapter Controller for Authority-C
 * 
 * This controller implements the standardized API contract endpoints
 * required by the frontend dashboard. It adapts internal service responses
 * to match the contract format.
 */
@RestController
@RequestMapping("/api")
public class AdapterController {

    private final WebClient web;
    private final UpstreamsProperties upstreams;
    private final ObjectMapper om;

    public AdapterController(WebClient.Builder builder, UpstreamsProperties ups, ObjectMapper om) {
        this.web = builder.build();
        this.upstreams = ups;
        this.om = om;
    }

    /**
     * GET /api/observations/count
     * Returns the total count of observations
     */
    @GetMapping("/observations/count")
    public ResponseEntity<?> getObservationCount(
            @RequestParam(name = "authority", required = false) String authority) {
        try {
            final URI uri = UriComponentsBuilder.fromHttpUrl(upstreams.getData())
                    .path("/observations/count")
                    .queryParamIfPresent("authority", java.util.Optional.ofNullable(authority))
                    .build(true)
                    .toUri();

            String responseJson = web.get().uri(uri).retrieve().bodyToMono(String.class).block();
            
            // Parse and return as-is if already in correct format, otherwise transform
            JsonNode responseNode = om.readTree(responseJson);
            Map<String, Object> response = new HashMap<>();
            
            if (responseNode.has("count")) {
                response.put("count", responseNode.get("count").asLong());
            } else {
                // If response is just a number
                response.put("count", responseNode.asLong());
            }
            
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
            final URI uri = UriComponentsBuilder.fromHttpUrl(upstreams.getData())
                    .path("/observations/latest")
                    .queryParam("limit", limit)
                    .queryParamIfPresent("authority", java.util.Optional.ofNullable(authority))
                    .build(true)
                    .toUri();

            String observationsJson = web.get().uri(uri).retrieve().bodyToMono(String.class).block();
            JsonNode observationsArray = om.readTree(observationsJson);
            
            List<Map<String, Object>> response = new ArrayList<>();
            
            if (observationsArray.isArray()) {
                for (JsonNode observation : observationsArray) {
                    Map<String, Object> transformed = transformObservation(observation);
                    response.add(transformed);
                }
            }

            // Sort by createdAt descending (newest first)
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

    // Note: /api/rewards/leaderboard is already handled by RewardsProxyController
    // The existing endpoint requires 'authority' parameter, which matches the contract
    // No duplicate endpoint needed here

    /**
     * Transforms an observation from Authority-C format to contract format
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
            if (measurementsNode.has("temperatureCelsius") || measurementsNode.has("temperature")) {
                double temp = measurementsNode.has("temperatureCelsius") 
                        ? measurementsNode.get("temperatureCelsius").asDouble()
                        : measurementsNode.get("temperature").asDouble();
                measurements.put("temperature", temp);
            }
            if (measurementsNode.has("ph")) {
                measurements.put("pH", measurementsNode.get("ph").asDouble());
            }
            if (measurementsNode.has("alkalinityMgPerL") || measurementsNode.has("alkalinity")) {
                double alk = measurementsNode.has("alkalinityMgPerL")
                        ? measurementsNode.get("alkalinityMgPerL").asDouble()
                        : measurementsNode.get("alkalinity").asDouble();
                measurements.put("alkalinity", alk);
            }
            if (measurementsNode.has("turbidityNtu") || measurementsNode.has("turbidity")) {
                double turb = measurementsNode.has("turbidityNtu")
                        ? measurementsNode.get("turbidityNtu").asDouble()
                        : measurementsNode.get("turbidity").asDouble();
                measurements.put("turbidity", turb);
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

