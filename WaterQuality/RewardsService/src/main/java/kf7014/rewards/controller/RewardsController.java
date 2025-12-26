package kf7014.rewards.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kf7014.rewards.model.RewardSummary;
import kf7014.rewards.service.RewardComputationService;

@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

    private final RewardComputationService service;

    public RewardsController(RewardComputationService service) {
        this.service = service;
    }

    @PostMapping("/recompute/{citizenId}")
    public ResponseEntity<RewardSummary> recompute(@PathVariable("citizenId") String citizenId) {
        return ResponseEntity.ok(service.recomputeForCitizen(citizenId));
    }

    @GetMapping("/{citizenId}")
    public ResponseEntity<RewardSummary> get(@PathVariable("citizenId") String citizenId) {
        return ResponseEntity.ok(service.getSummary(citizenId));
    }
}


