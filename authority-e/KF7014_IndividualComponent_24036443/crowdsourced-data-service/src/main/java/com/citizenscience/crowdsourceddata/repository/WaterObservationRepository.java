package com.citizenscience.crowdsourceddata.repository;

import com.citizenscience.crowdsourceddata.model.WaterObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for {@link WaterObservation} entities.
 *
 * Provides lookup helpers used by controllers and services when retrieving
 * observations for specific citizens.
 */
public interface WaterObservationRepository extends JpaRepository<WaterObservation, String> {

    /**
     * Returns all observations stored against the supplied citizen identifier.
     *
     * @param citizenId sequential citizen identifier
     * @return list of persisted observations for that citizen
     */
    List<WaterObservation> findByCitizenId(String citizenId);
}
