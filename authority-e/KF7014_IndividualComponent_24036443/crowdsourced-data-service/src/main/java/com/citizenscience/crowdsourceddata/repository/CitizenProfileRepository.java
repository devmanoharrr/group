package com.citizenscience.crowdsourceddata.repository;

import com.citizenscience.crowdsourceddata.model.CitizenProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for {@link CitizenProfile} persistence.
 *
 * Supports sequence number lookups so the service can generate predictable
 * citizen identifiers like {@code CTZ-003}.
 */
public interface CitizenProfileRepository extends JpaRepository<CitizenProfile, String> {

    /**
     * Finds the most recently created profile to determine the next sequence.
     *
     * @return optional containing the profile with the highest sequence number
     */
    Optional<CitizenProfile> findTopByOrderBySequenceNumberDesc();
}
