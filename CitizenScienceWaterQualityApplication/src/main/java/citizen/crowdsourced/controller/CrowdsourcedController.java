package citizen.crowdsourced.controller;

import citizen.crowdsourced.dto.CrowdsourcedRequest;
import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.service.CrowdsourcedService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crowdsourced")
public class CrowdsourcedController {

    private final CrowdsourcedService service;

    public CrowdsourcedController(CrowdsourcedService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public ResponseEntity<CrowdsourcedRecord> submit(@Valid @RequestBody CrowdsourcedRequest request) {
        CrowdsourcedRecord saved = service.submit(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CrowdsourcedRecord>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<CrowdsourcedRecord>> byCitizen(@PathVariable String citizenId) {
        return ResponseEntity.ok(service.findByCitizenId(citizenId));
    }
}
