package kf7014.crowdsourceddata.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import kf7014.crowdsourceddata.model.WaterObservation;
import kf7014.crowdsourceddata.service.WaterObservationService;

import java.util.List;

@RestController
@RequestMapping("/api/observations")
@Validated
public class WaterObservationController {

    private final WaterObservationService service;

    public WaterObservationController(WaterObservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WaterObservation> submit(@RequestBody WaterObservation observation) {
        WaterObservation saved = service.createObservation(observation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<WaterObservation>> list(@RequestParam(value = "citizenId", required = false) String citizenId) {
        if (citizenId != null && !citizenId.isBlank()) {
            return ResponseEntity.ok(service.listByCitizen(citizenId));
        }
        return ResponseEntity.ok(service.listAll());
    }
}


