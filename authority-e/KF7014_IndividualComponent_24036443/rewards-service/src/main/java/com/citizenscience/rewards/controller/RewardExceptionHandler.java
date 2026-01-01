package com.citizenscience.rewards.controller;

import com.citizenscience.rewards.exception.ObservationFetchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for the rewards service.
 *
 * Translates downstream fetch failures and invalid parameters into consistent
 * HTTP responses for API consumers.
 */
@RestControllerAdvice
public class RewardExceptionHandler {

    /**
     * Converts upstream communication failures into a 502 response.
     *
     * @param ex exception raised when the data service cannot be reached
     * @return HTTP 502 response entity with an error description
     */
    @ExceptionHandler(ObservationFetchException.class)
    public ResponseEntity<Map<String, String>> handleObservationFetch(ObservationFetchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles validation errors resulting from malformed input parameters.
     *
     * @param ex exception raised when arguments are invalid
     * @return HTTP 400 response entity with an error description
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
