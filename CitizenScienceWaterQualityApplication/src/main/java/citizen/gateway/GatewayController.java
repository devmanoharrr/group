package citizen.gateway;

import citizen.crowdsourced.dto.CrowdsourcedRequest;
import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.service.CrowdsourcedService;
import citizen.rewards.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private final CrowdsourcedService crowdsourcedService;
    private final RewardService rewardService;

    public GatewayController(CrowdsourcedService crowdsourcedService, RewardService rewardService) {
        this.crowdsourcedService = crowdsourcedService;
        this.rewardService = rewardService;
    }

    @PostMapping("/submit")
    public ResponseEntity<CrowdsourcedRecord> submit(@RequestBody CrowdsourcedRequest req) {
        CrowdsourcedRecord rec = crowdsourcedService.submit(req);
        rewardService.processAllNow();
        return ResponseEntity.ok(rec);
    }

    @GetMapping("/rewards/{citizenId}")
    public ResponseEntity<?> getRewards(@PathVariable String citizenId) {
        return ResponseEntity.ok(rewardService.getSummary(citizenId));
    }
}