package citizen.rewards.service;

import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.repository.CrowdsourcedRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RewardService {

    private final CrowdsourcedRepository repository;

    private final Map<String, Integer> points = new ConcurrentHashMap<>();

    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    public RewardService(CrowdsourcedRepository repository) {
        this.repository = repository;
    }

    private int pointsForRecord(CrowdsourcedRecord r) {
        int base = 10;
        boolean complete = isComplete(r);
        return base + (complete ? 10 : 0);
    }

    private boolean isComplete(CrowdsourcedRecord r) {
        boolean hasAllMeasurements = r.getTemperature() != null && r.getpH()!= null
                && r.getAlkalinity() != null && r.getTurbidity() != null;
        boolean hasObservation = r.getObservations() != null && !r.getObservations().isBlank();
        return hasAllMeasurements && hasObservation;
    }

    private void processRecord(CrowdsourcedRecord r) {
        if (r == null || processed.contains(r.getId())) return;
        int add = pointsForRecord(r);
        points.merge(r.getCitizenId(), add, Integer::sum);
        processed.add(r.getId());
    }

    @Scheduled(fixedDelay = 30000)
    public void processNewRecords() {
        List<CrowdsourcedRecord> all = repository.findAll();
        all.forEach(this::processRecord);
    }

    public Map<String, Object> getSummary(String citizenId) {
        int p = points.getOrDefault(citizenId, 0);
        String badge = badgeForPoints(p);
        Map<String, Object> m = new HashMap<>();
        m.put("citizenId", citizenId);
        m.put("points", p);
        m.put("badge", badge);
        return m;
    }

    private String badgeForPoints(int p) {
        if (p >= 500) return "Gold";
        if (p >= 200) return "Silver";
        if (p >= 100) return "Bronze";
        return "None";
    }

    public void processAllNow() {
        repository.findAll().forEach(this::processRecord);
    }
}