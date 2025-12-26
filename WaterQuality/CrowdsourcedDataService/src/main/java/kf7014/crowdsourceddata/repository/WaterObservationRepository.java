package kf7014.crowdsourceddata.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import kf7014.crowdsourceddata.model.WaterObservation;

import java.util.List;

public interface WaterObservationRepository extends MongoRepository<WaterObservation, String> {
    List<WaterObservation> findByCitizenId(String citizenId);
}


