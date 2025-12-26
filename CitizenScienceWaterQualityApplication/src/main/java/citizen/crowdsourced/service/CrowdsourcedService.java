package citizen.crowdsourced.service;

import citizen.crowdsourced.dto.CrowdsourcedRequest;
import org.springframework.stereotype.Service;
import citizen.crowdsourced.model.CrowdsourcedRecord;
import citizen.crowdsourced.repository.CrowdsourcedRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Service
public class CrowdsourcedService {
    private final CrowdsourcedRepository repo;

    public CrowdsourcedService(CrowdsourcedRepository repo) {
        this.repo = repo;
    }

    public CrowdsourcedRecord saveRecord(CrowdsourcedRecord record) {
        if (record.getPostcode() == null || 
            (record.getTemperature() == null && record.getObservations() == null)) {
            throw new IllegalArgumentException("Invalid data: postcode and at least one field required");
        }
        return repo.save(record);
    }

    public List<CrowdsourcedRecord> findAll() {
        return repo.findAll();
    }

    public List<CrowdsourcedRecord> findByCitizenId(String citizenId) {
        return repo.findByCitizenId(citizenId);
    }
    
    private void validateRequest(CrowdsourcedRequest req) {
        if (!StringUtils.hasText(req.getPostcode())) {
            throw new IllegalArgumentException("postcode is required");
        }
        boolean hasMeasurement = req.getTemperature() != null || req.getPh() != null
                || req.getAlkalinity() != null || req.getTurbidity() != null;
        boolean hasObservation = req.getObservations() != null && !req.getObservations().isEmpty();
        if (!hasMeasurement && !hasObservation) {
            throw new IllegalArgumentException("At least one measurement or one observation required");
        }
    }
    
    @Transactional
    public CrowdsourcedRecord submit(CrowdsourcedRequest req) {
        validateRequest(req);

        CrowdsourcedRecord record = new CrowdsourcedRecord();
        record.setCitizenId(req.getCitizenId());
        record.setPostcode(req.getPostcode());
        record.setTemperature(req.getTemperature());
        record.setpH(req.getPh());
        record.setAlkalinity(req.getAlkalinity());
        record.setTurbidity(req.getTurbidity());

        if (req.getObservations() != null && !req.getObservations().isEmpty()) {
            String obs = req.getObservations().stream().collect(Collectors.joining(","));
            record.setObservations(obs);
        }
        
        return repo.save(record);
    }
}