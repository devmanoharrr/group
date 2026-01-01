package com.citizenscience.crowdsourceddata.service;

import com.citizenscience.crowdsourceddata.model.CitizenProfile;
import com.citizenscience.crowdsourceddata.repository.CitizenProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business service for managing citizen profiles and identifiers.
 *
 * Generates sequential IDs, stores minimal metadata, and offers helper methods
 * that controllers and validators use to verify whether a citizen exists.
 */
@Service
public class CitizenService {

    /** Repository that stores the sequential numbering state. */
    private final CitizenProfileRepository repository;

    /**
     * Creates a service with its required repository.
     *
     * @param repository persistence interface for {@link CitizenProfile}
     */
    public CitizenService(CitizenProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Registers a new citizen and assigns the next sequential identifier.
     *
     * @return the newly persisted profile with ID and timestamps populated
     */
    @Transactional
    public CitizenProfile registerCitizen() {
        long nextSequence = repository.findTopByOrderBySequenceNumberDesc()
                .map(CitizenProfile::getSequenceNumber)
                .orElse(0L) + 1;
        CitizenProfile profile = CitizenProfile.create(nextSequence);
        return repository.save(profile);
    }

    /**
     * Checks whether a citizen identifier is already registered.
     *
     * @param citizenId identifier from API requests
     * @return {@code true} when the profile exists in storage
     */
    @Transactional(readOnly = true)
    public boolean exists(String citizenId) {
        return repository.existsById(citizenId);
    }
}
