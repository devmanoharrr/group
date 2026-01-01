package com.waterquality.data.exception;

/**
 * Custom exception classes for the Crowdsourced Data Service.
 * 
 * These exceptions provide specific error handling for various business logic
 * and validation scenarios in the application.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
public class CustomExceptions {

    /**
     * Exception thrown when submission validation fails.
     * This occurs when the submission doesn't meet the minimum requirements
     * (must have postcode AND at least one measurement or observation).
     */
    public static class InvalidSubmissionException extends RuntimeException {
        public InvalidSubmissionException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when an observation is not found by its ID.
     */
    public static class ObservationNotFoundException extends RuntimeException {
        public ObservationNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when too many images are uploaded (maximum 3 allowed).
     */
    public static class TooManyImagesException extends RuntimeException {
        public TooManyImagesException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when an invalid observation type is provided.
     * Valid values: Clear, Cloudy, Murky, Foamy, Oily, Discoloured, Presence of Odour.
     */
    public static class InvalidObservationTypeException extends RuntimeException {
        public InvalidObservationTypeException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when database operations fail.
     */
    public static class DatabaseException extends RuntimeException {
        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
