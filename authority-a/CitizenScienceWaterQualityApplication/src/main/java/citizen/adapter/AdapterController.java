package citizen.adapter;

import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.repository.CrowdsourcedRepository;
import citizen.rewards.service.RewardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API Adapter Controller for Authority-A
 * 
 * This controller implements the standardized API contract endpoints
 * required by the frontend dashboard. It adapts internal data structures
 * to match the contract format.
 */
@RestController
@RequestMapping("/api")
public class AdapterController {

    private final CrowdsourcedRepository crowdsourcedRepository;
    private final RewardService rewardService;

    public AdapterController(CrowdsourcedRepository crowdsourcedRepository, RewardService rewardService) {
        this.crowdsourcedRepository = crowdsourcedRepository;
        this.rewardService = rewardService;
    }

    /**
     * GET /api/observations/count
     * Returns the total count of observations
     */
    @GetMapping("/observations/count")
    public ResponseEntity<Map<String, Object>> getObservationCount(
            @RequestParam(required = false) String authority) {
        try {
            long count = crowdsourcedRepository.count();
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
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
            @RequestParam(required = false, defaultValue = "5") int limit,
            @RequestParam(required = false) String authority) {
        
        // Validate limit parameter
        if (limit < 1 || limit > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            "Invalid limit parameter. Must be between 1 and 50.",
                            "/api/observations/recent"));
        }

        try {
            List<CrowdsourcedRecord> allRecords = crowdsourcedRepository.findAll();
            
            // Sort by timestamp descending (newest first)
            List<CrowdsourcedRecord> sortedRecords = allRecords.stream()
                    .sorted(Comparator.comparing(CrowdsourcedRecord::getTimestamp).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            // Transform to contract format
            List<Map<String, Object>> response = sortedRecords.stream()
                    .map(this::transformToContractFormat)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
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
            @RequestParam(required = false, defaultValue = "3") int limit,
            @RequestParam(required = false) String authority) {
        
        // Validate limit parameter
        if (limit < 1 || limit > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            "Invalid limit parameter. Must be between 1 and 50.",
                            "/api/rewards/leaderboard"));
        }

        try {
            // Process all records to ensure points are up to date
            rewardService.processAllNow();
            
            // Get all citizen IDs from records
            List<CrowdsourcedRecord> allRecords = crowdsourcedRepository.findAll();
            Set<String> citizenIds = allRecords.stream()
                    .map(CrowdsourcedRecord::getCitizenId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Build leaderboard entries
            List<Map<String, Object>> leaderboard = citizenIds.stream()
                    .map(citizenId -> {
                        Map<String, Object> summary = rewardService.getSummary(citizenId);
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("contributorId", summary.get("citizenId"));
                        entry.put("points", summary.get("points"));
                        return entry;
                    })
                    .sorted((a, b) -> {
                        Integer pointsA = (Integer) a.get("points");
                        Integer pointsB = (Integer) b.get("points");
                        return pointsB.compareTo(pointsA); // Descending order
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            // Add rank numbers
            for (int i = 0; i < leaderboard.size(); i++) {
                leaderboard.get(i).put("rank", i + 1);
            }

            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal Server Error",
                            "Failed to retrieve leaderboard: " + e.getMessage(),
                            "/api/rewards/leaderboard"));
        }
    }

    /**
     * Transforms a CrowdsourcedRecord to the contract format
     */
    private Map<String, Object> transformToContractFormat(CrowdsourcedRecord record) {
        Map<String, Object> result = new HashMap<>();
        
        // Postcode
        result.put("postcode", record.getPostcode());
        
        // Measurements object
        Map<String, Object> measurements = new HashMap<>();
        if (record.getTemperature() != null) {
            measurements.put("temperature", record.getTemperature());
        }
        if (record.getpH() != null) {
            measurements.put("pH", record.getpH());
        }
        if (record.getAlkalinity() != null) {
            measurements.put("alkalinity", record.getAlkalinity());
        }
        if (record.getTurbidity() != null) {
            measurements.put("turbidity", record.getTurbidity());
        }
        result.put("measurements", measurements);
        
        // Observation text
        result.put("observation", record.getObservations());
        
        // CreatedAt (ISO 8601 format)
        if (record.getTimestamp() != null) {
            ZonedDateTime zonedDateTime = record.getTimestamp().atZone(ZoneId.systemDefault());
            result.put("createdAt", zonedDateTime.format(DateTimeFormatter.ISO_INSTANT));
        }
        
        // Contributor ID
        result.put("contributorId", record.getCitizenId());
        
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

