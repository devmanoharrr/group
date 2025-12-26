package citizen.rewards.controller;

import citizen.rewards.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/{citizenId}")
    public ResponseEntity<Map<String, Object>> getRewards(@PathVariable String citizenId) {
        return ResponseEntity.ok(rewardService.getSummary(citizenId));
    }

    @PostMapping("/processAll")
    public ResponseEntity<String> processAll() {
        rewardService.processAllNow();
        return ResponseEntity.ok("Processing triggered");
    }
}