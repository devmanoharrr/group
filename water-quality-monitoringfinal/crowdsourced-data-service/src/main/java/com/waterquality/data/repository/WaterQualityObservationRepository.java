package com.waterquality.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.waterquality.data.model.entity.WaterQualityObservation;

import java.util.List;

/**
 * Repository interface for WaterQualityObservation entity.
 * 
 * This interface extends JpaRepository to provide CRUD operations and custom queries
 * for managing water quality observations in the database.
 * 
 * Spring Data JPA automatically implements this interface at runtime, providing
 * standard database operations without requiring explicit implementation.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Repository
public interface WaterQualityObservationRepository extends JpaRepository<WaterQualityObservation, String> {

    /**
     * Finds all observations submitted by a specific citizen.
     * 
     * @param citizenId the unique identifier of the citizen
     * @return list of observations submitted by the specified citizen
     */
    List<WaterQualityObservation> findByCitizenId(String citizenId);

    /**
     * Finds all observations for a specific postcode.
     * 
     * @param postcode the postcode to search for
     * @return list of observations from the specified postcode
     */
    List<WaterQualityObservation> findByPostcode(String postcode);

    /**
     * Finds all unprocessed observations (not yet processed by Rewards Service).
     * 
     * @param processed the processed status (false for unprocessed)
     * @return list of unprocessed observations
     */
    List<WaterQualityObservation> findByProcessed(Boolean processed);

    /**
     * Finds unprocessed observations for a specific citizen.
     * 
     * @param citizenId the unique identifier of the citizen
     * @param processed the processed status
     * @return list of unprocessed observations for the citizen
     */
    List<WaterQualityObservation> findByCitizenIdAndProcessed(String citizenId, Boolean processed);

    /**
     * Counts the number of observations that have been processed.
     * 
     * @param processed the processed status (true for processed observations)
     * @return count of processed observations
     */
    Long countByProcessed(Boolean processed);

    /**
     * Custom query to get the most recent observations, ordered by timestamp descending.
     * 
     * @return list of all observations ordered by most recent first
     */
    @Query("SELECT o FROM WaterQualityObservation o ORDER BY o.timestamp DESC")
    List<WaterQualityObservation> findAllOrderByTimestampDesc();

    /**
     * Custom query to get observations by citizen ordered by timestamp.
     * 
     * @param citizenId the unique identifier of the citizen
     * @return list of citizen's observations ordered by timestamp descending
     */
    @Query("SELECT o FROM WaterQualityObservation o WHERE o.citizenId = ?1 ORDER BY o.timestamp DESC")
    List<WaterQualityObservation> findByCitizenIdOrderByTimestampDesc(String citizenId);
}
