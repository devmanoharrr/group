package com.citizenscience.crowdsourceddata.exception;

/**
 * Exception thrown when an observation references an unknown citizen ID.
 */
public class CitizenNotFoundException extends RuntimeException {
    /**
     * Builds the exception with a helpful error message.
     *
     * @param citizenId identifier that could not be located
     */
    public CitizenNotFoundException(String citizenId) {
        super("Citizen with id " + citizenId + " was not found");
    }
}
