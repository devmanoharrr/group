package com.bharath.wq.gateway.api;

import com.bharath.wq.gateway.config.UpstreamsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST controller for proxying requests to the Data Service.
 *
 * <p>Validates requests before forwarding them to the upstream Data Service. Handles both JSON and
 * multipart form data requests.
 */
@RestController
@RequestMapping("/api/data")
public class DataProxyController {

  private static final Logger log = LoggerFactory.getLogger(DataProxyController.class);
  private static final int MAX_JSON_SIZE = 100_000; // 100KB
  private static final int MAX_IMAGES = 3;

  private final WebClient web;
  private final UpstreamsProperties upstreams;
  private final ObjectMapper om;

  /**
   * Constructs a new DataProxyController.
   *
   * @param builder WebClient builder
   * @param ups upstream service properties
   * @param om JSON object mapper
   */
  public DataProxyController(WebClient.Builder builder, UpstreamsProperties ups, ObjectMapper om) {
    this.web = builder.build();
    this.upstreams = ups;
    this.om = om;
  }

  @GetMapping("/observations/latest")
  public ResponseEntity<String> latest(
      @RequestParam(name = "authority", required = false) String authority,
      @RequestParam(name = "limit", defaultValue = "5") int limit) {

    final URI uri =
        UriComponentsBuilder.fromHttpUrl(upstreams.getData())
            .path("/observations/latest")
            .queryParam("limit", limit)
            .queryParamIfPresent("authority", java.util.Optional.ofNullable(authority))
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

  @GetMapping("/observations/{id}")
  public ResponseEntity<String> getById(@PathVariable("id") String id) {
    final URI uri = URI.create(upstreams.getData() + "/observations/" + id);
    try {
      return web.get().uri(uri).retrieve().toEntity(String.class).block();
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (org.springframework.web.reactive.function.client.WebClientException e) {
      throw e; // Let GatewayErrorAdvice handle it
    }
  }

  @GetMapping("/observations/count")
  public ResponseEntity<String> count(
      @RequestParam(name = "authority", required = false) String authority) {

    final URI uri =
        UriComponentsBuilder.fromHttpUrl(upstreams.getData())
            .path("/observations/count")
            .queryParamIfPresent("authority", java.util.Optional.ofNullable(authority))
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

  @PostMapping(path = "/observations", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> create(@RequestBody String json) {
    // Validate request size
    if (json != null && json.length() > MAX_JSON_SIZE) {
      log.warn("Request body too large: {} bytes", json.length());
      throw new IllegalArgumentException(
          "Request body exceeds maximum size of " + MAX_JSON_SIZE + " bytes");
    }

    log.debug("Proxying POST /observations to Data Service");
    final URI uri = URI.create(upstreams.getData() + "/observations");
    try {
      return web.post()
          .uri(uri)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(json)
          .retrieve()
          .toEntity(String.class)
          .block();
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (org.springframework.web.reactive.function.client.WebClientException e) {
      throw e; // Let GatewayErrorAdvice handle it
    }
  }

  @PostMapping(
      path = "/observations/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> upload(
      @RequestPart("payload") String payloadJson,
      @RequestPart(name = "images", required = false) List<MultipartFile> images) {

    // Validate request size
    if (payloadJson != null && payloadJson.length() > MAX_JSON_SIZE) {
      log.warn("Payload JSON too large: {} bytes", payloadJson.length());
      throw new IllegalArgumentException(
          "Payload exceeds maximum size of " + MAX_JSON_SIZE + " bytes");
    }

    // Validate image count
    if (images != null && images.size() > MAX_IMAGES) {
      log.warn("Too many images: {} (max: {})", images.size(), MAX_IMAGES);
      throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images allowed");
    }

    log.debug(
        "Proxying POST /observations/upload with {} images to Data Service",
        images != null ? images.size() : 0);

    final MultipartBodyBuilder mb = new MultipartBodyBuilder();
    mb.part("payload", payloadJson).contentType(MediaType.APPLICATION_JSON);

    if (images != null) {
      for (final MultipartFile mf : images) {
        if (mf == null || mf.isEmpty()) {
          continue;
        }
        mb.part("images", mf.getResource())
            .contentType(
                MediaType.parseMediaType(
                    mf.getContentType() == null ? "application/octet-stream" : mf.getContentType()))
            .filename(mf.getOriginalFilename());
      }
    }

    final MultiValueMap<String, org.springframework.http.HttpEntity<?>> body = mb.build();

    try {
      final ResponseEntity<String> upstream =
          web.post()
          .uri(upstreams.getData() + "/observations/upload")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(BodyInserters.fromMultipartData(body))
          .retrieve()
          .toEntity(String.class)
          .block();
      return sanitize(upstream);
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      throw e; // Let GatewayErrorAdvice handle it
    } catch (org.springframework.web.reactive.function.client.WebClientException e) {
      throw e; // Let GatewayErrorAdvice handle it
    }
  }

  private static ResponseEntity<String> sanitize(ResponseEntity<String> upstream) {
    if (upstream == null) {
      return ResponseEntity.internalServerError().build();
    }
    final HttpHeaders headers = new HttpHeaders();
    headers.putAll(upstream.getHeaders());
    headers.remove(HttpHeaders.TRANSFER_ENCODING);
    headers.remove(HttpHeaders.CONTENT_LENGTH);
    headers.remove(HttpHeaders.CONNECTION);
    return ResponseEntity.status(upstream.getStatusCode()).headers(headers).body(upstream.getBody());
  }
}
