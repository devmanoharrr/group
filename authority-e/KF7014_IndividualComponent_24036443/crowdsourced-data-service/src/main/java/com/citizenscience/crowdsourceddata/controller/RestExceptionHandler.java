package com.citizenscience.crowdsourceddata.controller;

import com.citizenscience.crowdsourceddata.exception.CitizenNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception translator for the Crowdsourced Data service.
 *
 * Captures common validation and lookup failures and converts them into
 * user-friendly JSON responses so clients can react appropriately.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * Converts bean validation errors into a map keyed by field name.
     *
     * @param ex validation exception raised by {@code @Valid}
     * @return HTTP 400 containing field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Returns a standardised error envelope for rejected input.
     *
     * @param ex thrown when custom validation logic fails
     * @return HTTP 400 with an informative error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Maps missing citizen profiles to a not-found status code.
     *
     * @param ex thrown when a citizen identifier cannot be resolved
     * @return HTTP 404 with a helpful error payload
     */
    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCitizenNotFound(CitizenNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }
}
