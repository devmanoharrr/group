package kf7014.crowdsourceddata.service;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import kf7014.crowdsourceddata.model.MeasurementSet;
import kf7014.crowdsourceddata.model.WaterObservation;
import kf7014.crowdsourceddata.repository.WaterObservationRepository;

import java.util.List;

@Service
public class WaterObservationService {

    private final WaterObservationRepository repository;

    public WaterObservationService(WaterObservationRepository repository) {
        this.repository = repository;
    }

    public WaterObservation createObservation(WaterObservation observation) {
        validateObservation(observation);
        enforceImageLimit(observation);
        return repository.save(observation);
    }

    public List<WaterObservation> listAll() {
        return repository.findAll();
    }

    public List<WaterObservation> listByCitizen(String citizenId) {
        return repository.findByCitizenId(citizenId);
    }

    private void validateObservation(WaterObservation observation) {
        if (observation.getPostcode() == null || observation.getPostcode().isBlank()) {
            throw new IllegalArgumentException("Postcode is required");
        }
        boolean hasMeasurement = hasAnyMeasurement(observation.getMeasurements());
        boolean hasObservation = !CollectionUtils.isEmpty(observation.getObservations());
        if (!hasMeasurement && !hasObservation) {
            throw new IllegalArgumentException("At least one measurement or observation is required");
        }
        if (observation.getCitizenId() == null || observation.getCitizenId().isBlank()) {
            throw new IllegalArgumentException("Citizen ID is required");
        }
    }

    private boolean hasAnyMeasurement(MeasurementSet set) {
        if (set == null) return false;
        return set.getTemperatureCelsius() != null ||
                set.getPh() != null ||
                set.getAlkalinityMgPerL() != null ||
                set.getTurbidityNtu() != null;
    }

    private void enforceImageLimit(WaterObservation observation) {
        if (observation.getImageData() != null && observation.getImageData().size() > 3) {
            observation.setImageData(observation.getImageData().subList(0, 3));
        }
    }
}


