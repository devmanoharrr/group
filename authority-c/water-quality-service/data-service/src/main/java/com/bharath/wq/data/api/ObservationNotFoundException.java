package com.bharath.wq.data.api;

/**
 * Exception thrown when an observation is not found in the database.
 *
 * <p>This exception is used to signal that a requested observation ID does not exist, which should
 * result in a 404 Not Found HTTP response.
 */
public class ObservationNotFoundException extends RuntimeException {

  public ObservationNotFoundException(String message) {
    super(message);
  }
}
