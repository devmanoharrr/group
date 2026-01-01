package com.citizenscience.crowdsourceddata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity that represents a registered citizen within the data service.
 *
 * Stores the sequential identifier, the numeric sequence backing it, and the
 * timestamp when the profile was created so observation records can reference
 * a stable owner.
 */
@Entity
@Table(name = "citizen_profile")
public class CitizenProfile {

    /** Sequential citizen identifier presented to API clients. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /** Numeric counter used to generate new identifiers. */
    @Column(name = "sequence_number", nullable = false, updatable = false, unique = true)
    private long sequenceNumber;

    /** Timestamp of when the citizen profile was first created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Framework-friendly constructor required by JPA.
     */
    public CitizenProfile() {
        // JPA requires a default constructor
    }

    /**
     * Creates a profile with explicit values, used primarily by factory
     * methods.
     *
     * @param id             human-readable identifier such as {@code CTZ-001}
     * @param sequenceNumber numeric sequence for ordering
     * @param createdAt      timestamp of registration
     */
    public CitizenProfile(String id, long sequenceNumber, LocalDateTime createdAt) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.createdAt = createdAt;
    }

    /**
     * Factory method that builds a profile for the supplied sequence number.
     *
     * @param sequenceNumber numeric sequence to convert into a citizen ID
     * @return a fully initialised {@link CitizenProfile}
     */
    public static CitizenProfile create(long sequenceNumber) {
        String formattedId = formatIdentifier(sequenceNumber);
        return new CitizenProfile(formattedId, sequenceNumber, LocalDateTime.now());
    }

    private static String formatIdentifier(long sequenceNumber) {
        return String.format("CTZ-%03d", sequenceNumber);
    }

    /**
     * @return the citizen identifier exposed in API responses
     */
    public String getId() {
        return id;
    }

    /**
     * @return the numeric sequence backing the citizen identifier
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * @return timestamp describing when the citizen registered
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
