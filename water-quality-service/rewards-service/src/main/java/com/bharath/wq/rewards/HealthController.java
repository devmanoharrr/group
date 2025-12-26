package com.bharath.wq.rewards;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {
  @GetMapping("/healthz")
  Map<String, String> health() {
    return Map.of("service", "rewards-service", "status", "OK");
  }
}
