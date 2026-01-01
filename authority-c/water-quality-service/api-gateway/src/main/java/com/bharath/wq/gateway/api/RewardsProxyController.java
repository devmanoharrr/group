package com.bharath.wq.gateway.api;

import com.bharath.wq.gateway.config.UpstreamsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/rewards")
public class RewardsProxyController {

  private final WebClient web;
  private final UpstreamsProperties upstreams;
  private final ObjectMapper objectMapper;

  public RewardsProxyController(WebClient.Builder builder, UpstreamsProperties ups, ObjectMapper om) {
    this.web = builder.build();
    this.upstreams = ups;
    this.objectMapper = om;
  }

  @PostMapping("/ingest")
  public ResponseEntity<String> ingest(@RequestBody String json) {
    final URI uri = URI.create(upstreams.getRewards() + "/rewards/ingest");
    try {
      return web.post().uri(uri).bodyValue(json).retrieve().toEntity(String.class).block();
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (org.springframework.web.reactive.function.client.WebClientException e) {
      throw e; // Let GatewayErrorAdvice handle it
    }
  }

  @GetMapping("/citizens/{id}")
  public ResponseEntity<String> getTotals(@PathVariable("id") String id) {
    final URI uri = URI.create(upstreams.getRewards() + "/rewards/citizens/" + id);
    try {
      return web.get().uri(uri).retrieve().toEntity(String.class).block();
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (org.springframework.web.reactive.function.client.WebClientException e) {
      throw e; // Let GatewayErrorAdvice handle it
    }
  }

  @GetMapping("/leaderboard")
  public ResponseEntity<?> leaderboard(
      @RequestParam(name = "authority", required = false) String authority,
      @RequestParam(name = "limit", defaultValue = "3") int limit) {
    
    // Validate limit parameter
    if (limit < 1 || limit > 50) {
      Map<String, Object> error = new HashMap<>();
      error.put("status", HttpStatus.BAD_REQUEST.value());
      error.put("error", "Bad Request");
      error.put("message", "Invalid limit parameter. Must be between 1 and 50.");
      error.put("path", "/api/rewards/leaderboard");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // Use default authority if not provided
    if (authority == null || authority.isEmpty()) {
      authority = "authority-c";
    }

    final URI uri =
        UriComponentsBuilder.fromHttpUrl(upstreams.getRewards())
            .path("/rewards/leaderboard")
            .queryParam("authority", authority)
            .queryParam("limit", limit)
            .build(true)
            .toUri();

    try {
      String responseJson = web.get().uri(uri).retrieve().bodyToMono(String.class).block();
      
      // Transform response to contract format
      JsonNode leaderboardArray = objectMapper.readTree(responseJson);
      List<Map<String, Object>> response = new ArrayList<>();
      
      if (leaderboardArray.isArray()) {
        int rank = 1;
        for (JsonNode entry : leaderboardArray) {
          Map<String, Object> transformed = new HashMap<>();
          transformed.put("contributorId", entry.has("citizenId") 
              ? entry.get("citizenId").asText() 
              : "");
          transformed.put("points", entry.has("points") ? entry.get("points").asLong() : 0);
          transformed.put("rank", rank++);
          response.add(transformed);
        }
      }
      
      return ResponseEntity.ok(response);
    } catch (WebClientResponseException.ServiceUnavailable | 
             WebClientResponseException.GatewayTimeout e) {
      Map<String, Object> error = new HashMap<>();
      error.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
      error.put("error", "Service Unavailable");
      error.put("message", "Rewards service is currently unavailable");
      error.put("path", "/api/rewards/leaderboard");
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (org.springframework.web.reactive.function.client.WebClientException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (Exception e) {
      Map<String, Object> error = new HashMap<>();
      error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
      error.put("error", "Internal Server Error");
      error.put("message", "Failed to retrieve leaderboard: " + e.getMessage());
      error.put("path", "/api/rewards/leaderboard");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }
}
