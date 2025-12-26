package com.bharath.wq.rewards.service;

import com.bharath.wq.rewards.model.ObservationDTO;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class DataClient {
  private final WebClient client;
  private final int batchSize;

  public DataClient(
      WebClient.Builder builder,
      @Value("${rewards.data.base-url}") String baseUrl,
      @Value("${rewards.data.batch-size:50}") int batchSize) {
    this.client = builder.baseUrl(baseUrl).build();
    this.batchSize = batchSize;
  }

  public List<ObservationDTO> fetchLatest() {
    return client
        .get()
        .uri(uri -> uri.path("/observations/latest").queryParam("limit", batchSize).build())
        .retrieve()
        .bodyToFlux(ObservationDTO.class)
        .collectList()
        .block(Duration.ofSeconds(5));
  }
}
