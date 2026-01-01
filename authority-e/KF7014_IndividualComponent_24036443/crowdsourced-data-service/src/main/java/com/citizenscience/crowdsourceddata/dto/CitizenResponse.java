package com.citizenscience.crowdsourceddata.dto;

import java.time.LocalDateTime;

/**
 * Response DTO returned when a citizen profile is created.
 *
 * Conveys the generated identifier and the registration timestamp back to the
 * client so it can be reused for subsequent observation submissions.
 */
public class CitizenResponse {

    /** Generated citizen identifier such as {@code CTZ-001}. */
    private final String id;
    /** Timestamp describing when the profile was created. */
    private final LocalDateTime createdAt;

    /**
     * Builds an immutable response containing citizen details.
     *
     * @param id        sequential citizen identifier
     * @param createdAt creation timestamp of the citizen profile
     */
    public CitizenResponse(String id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    /**
     * @return the generated citizen identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return the moment the citizen profile was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
