package com.bharath.wq.rewards.api;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the Rewards Service API.
 *
 * <p>Handles various exceptions and converts them to appropriate HTTP responses with ProblemDetail
 * format (RFC 7807).
 */
@RestControllerAdvice
public class RewardsErrorAdvice {

  private static final Logger log = LoggerFactory.getLogger(RewardsErrorAdvice.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail onIllegalArg(IllegalArgumentException ex) {
    log.warn("Invalid input: {}", ex.getMessage());
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Invalid input");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail onBeanValidation(MethodArgumentNotValidException ex) {
    log.warn("Validation failed: {}", ex.getBindingResult());
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Validation failed");
    pd.setDetail(ex.getBindingResult().toString());
    return pd;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail onConstraint(ConstraintViolationException ex) {
    log.warn("Constraint violation: {}", ex.getMessage());
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Constraint violation");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail onJsonParse(HttpMessageNotReadableException ex) {
    log.warn("Invalid request body: {}", ex.getMessage());
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Invalid request body");
    pd.setDetail(
        ex.getMostSpecificCause() != null
            ? ex.getMostSpecificCause().getMessage()
            : ex.getMessage());
    return pd;
  }

  /**
   * Catch-all handler for unexpected exceptions (500).
   *
   * @param ex the exception
   * @return ProblemDetail with 500 status
   */
  @ExceptionHandler(Exception.class)
  public ProblemDetail onGenericException(Exception ex) {
    log.error("Unexpected error occurred", ex);
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal server error");
    pd.setDetail("An unexpected error occurred. Please try again later.");
    return pd;
  }
}
