package com.bharath.wq.rewards.api;

import com.bharath.wq.rewards.model.LeaderboardRow;
import com.bharath.wq.rewards.model.ObservationEvent;
import com.bharath.wq.rewards.model.RewardTotals;
import com.bharath.wq.rewards.service.RewardsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rewards")
public class RewardsController {

  private final RewardsService service;

  public RewardsController(RewardsService service) {
    this.service = service;
  }

  /** Manually ingest an observation (used until we wire automatic sync). */
  @PostMapping("/ingest")
  public ResponseEntity<ProblemDetail> ingest(@Valid @RequestBody ObservationEvent event) {
    service.ingest(event);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.ACCEPTED, "ingested"));
  }

  @GetMapping("/citizens/{id}")
  public RewardTotals getTotals(@PathVariable("id") String citizenId) {
    return service.getTotals(citizenId);
  }

  @GetMapping("/leaderboard")
  public List<LeaderboardRow> leaderboard(
      @RequestParam(name = "authority") String authority,
      @RequestParam(name = "limit", defaultValue = "3") int limit) {
    return service.leaderboard(authority, limit);
  }
}
