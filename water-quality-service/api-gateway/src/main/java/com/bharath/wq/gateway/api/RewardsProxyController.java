package com.bharath.wq.gateway.api;

import com.bharath.wq.gateway.config.UpstreamsProperties;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/rewards")
public class RewardsProxyController {

  private final WebClient web;
  private final UpstreamsProperties upstreams;

  public RewardsProxyController(WebClient.Builder builder, UpstreamsProperties ups) {
    this.web = builder.build();
    this.upstreams = ups;
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
  public ResponseEntity<String> leaderboard(
      @RequestParam(name = "authority") String authority,
      @RequestParam(name = "limit", defaultValue = "3") int limit) {

    final URI uri =
        UriComponentsBuilder.fromHttpUrl(upstreams.getRewards())
            .path("/rewards/leaderboard")
            .queryParam("authority", authority)
            .queryParam("limit", limit)
            .build(true)
            .toUri();

    try {
      return web.get().uri(uri).retrieve().toEntity(String.class).block();
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (org.springframework.web.reactive.function.client.WebClientException e) {
      throw e; // Let GatewayErrorAdvice handle it
    }
  }
}
