package com.citizenscience.rewards.exception;

/**
 * Signals that the rewards service could not retrieve observations from the
 * Crowdsourced Data microservice.
 */
public class ObservationFetchException extends RuntimeException {

    /**
     * Creates an exception with an explanatory message and root cause.
     *
     * @param message description of the failure
     * @param cause   underlying exception thrown by the HTTP client
     */
    public ObservationFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception with only a message when no cause is available.
     *
     * @param message description of the failure
     */
    public ObservationFetchException(String message) {
        super(message);
    }
}
