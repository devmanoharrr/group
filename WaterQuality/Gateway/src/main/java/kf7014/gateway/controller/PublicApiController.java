package kf7014.gateway.controller;

import kf7014.gateway.client.DownstreamClients;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final DownstreamClients clients;

    public PublicApiController(DownstreamClients clients) {
        this.clients = clients;
    }

    @PostMapping(path = "/observations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> createObservation(@RequestBody String body) {
        return clients.createObservation(body);
    }

    @GetMapping(path = "/observations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> listObservations(@RequestParam(value = "citizenId", required = false) String citizenId) {
        return clients.listObservations(citizenId);
    }

    @PostMapping(path = "/rewards/recompute/{citizenId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> recompute(@PathVariable("citizenId") String citizenId) {
        return clients.recomputeRewards(citizenId);
    }

    @GetMapping(path = "/rewards/{citizenId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> get(@PathVariable("citizenId") String citizenId) {
        return clients.getRewards(citizenId);
    }
}


