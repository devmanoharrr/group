package com.waterquality.data.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.waterquality.data.model.dto.ResponseDTOs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the Crowdsourced Data Service.
 * 
 * This class handles all exceptions thrown in the application and converts them
 * into appropriate HTTP responses with error details.
 * 
 * Using @ControllerAdvice, this handler intercepts exceptions across all controllers
 * and provides consistent error response formatting.
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions when request body validation fails.
     * 
     * @param ex the validation exception
     * @return ResponseEntity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTOs.ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ResponseDTOs.ErrorResponse errorResponse = ResponseDTOs.ErrorResponse.builder()
                .status("validation_failed")
                .message("Request validation failed")
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .details(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles invalid submission exceptions.
     * 
     * @param ex the invalid submission exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(CustomExceptions.InvalidSubmissionException.class)
    public ResponseEntity<ResponseDTOs.ErrorResponse> handleInvalidSubmission(
            CustomExceptions.InvalidSubmissionException ex) {
        
        ResponseDTOs.ErrorResponse errorResponse = ResponseDTOs.ErrorResponse.builder()
                .status("error")
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles observation not found exceptions.
     * 
     * @param ex the observation not found exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(CustomExceptions.ObservationNotFoundException.class)
    public ResponseEntity<ResponseDTOs.ErrorResponse> handleObservationNotFound(
            CustomExceptions.ObservationNotFoundException ex) {
        
        ResponseDTOs.ErrorResponse errorResponse = ResponseDTOs.ErrorResponse.builder()
                .status("error")
                .message(ex.getMessage())
                .code(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles too many images exception.
     * 
     * @param ex the too many images exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(CustomExceptions.TooManyImagesException.class)
    public ResponseEntity<ResponseDTOs.ErrorResponse> handleTooManyImages(
            CustomExceptions.TooManyImagesException ex) {
        
        ResponseDTOs.ErrorResponse errorResponse = ResponseDTOs.ErrorResponse.builder()
                .status("error")
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles invalid observation type exception.
     * 
     * @param ex the invalid observation type exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(CustomExceptions.InvalidObservationTypeException.class)
    public ResponseEntity<ResponseDTOs.ErrorResponse> handleInvalidObservationType(
            CustomExceptions.InvalidObservationTypeException ex) {
        
        ResponseDTOs.ErrorResponse errorResponse = ResponseDTOs.ErrorResponse.builder()
                .status("error")
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles database exceptions.
     * 
     * @param ex the database exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(CustomExceptions.DatabaseException.class)
    public ResponseEntity<ResponseDTOs.ErrorResponse> handleDatabaseException(
            CustomExceptions.DatabaseException ex) {
        
        ResponseDTOs.ErrorResponse errorResponse = ResponseDTOs.ErrorResponse.builder()
                .status("error")
                .message("Database operation failed: " + ex.getMessage())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles all other unexpected exceptions.
     * 
     * @param ex the exception
     * @return ResponseEntity with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTOs.ErrorResponse> handleGenericException(Exception ex) {
        
        ResponseDTOs.ErrorResponse errorResponse = ResponseDTOs.ErrorResponse.builder()
                .status("error")
                .message("An unexpected error occurred: " + ex.getMessage())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
