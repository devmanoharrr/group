package com.citizenscience.crowdsourceddata.model;

/**
 * Enumeration of supported qualitative water observations.
 *
 * Values map directly to user input allowing the service to normalise free text
 * into a controlled vocabulary consumed by reward calculations.
 */
public enum ObservationCondition {
    /** Water is clear and transparent. */
    CLEAR,
    /** Water appears cloudy. */
    CLOUDY,
    /** Water has a murky or muddy appearance. */
    MURKY,
    /** Foam is present on the surface. */
    FOAMY,
    /** Oil slick or residue is visible. */
    OILY,
    /** Water colour deviates noticeably from normal. */
    DISCOLOURED,
    /** Detectable odour accompanies the sample. */
    PRESENCE_OF_ODOUR;

    /**
     * Normalises user input into the matching enum constant.
     *
     * @param value raw string entered by the citizen
     * @return matching {@link ObservationCondition}
     */
    public static ObservationCondition fromString(String value) {
        return ObservationCondition.valueOf(value.trim().toUpperCase().replace(' ', '_'));
    }
}
