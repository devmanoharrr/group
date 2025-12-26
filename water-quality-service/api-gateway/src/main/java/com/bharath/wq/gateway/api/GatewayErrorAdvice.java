package com.bharath.wq.gateway.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Global exception handler for the API Gateway.
 *
 * <p>Handles errors from upstream services and converts them to appropriate HTTP responses with
 * ProblemDetail format (RFC 7807).
 */
@RestControllerAdvice
public class GatewayErrorAdvice {

  private static final Logger log = LoggerFactory.getLogger(GatewayErrorAdvice.class);

  /**
   * Handle validation errors (e.g., request size limits).
   *
   * @param ex the IllegalArgumentException
   * @return ProblemDetail with 400 status
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail onValidationError(IllegalArgumentException ex) {
    log.warn("Validation error: {}", ex.getMessage());
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Validation failed");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  /**
   * Handle errors from upstream services (Data Service, Rewards Service).
   *
   * @param ex the WebClient exception
   * @return ProblemDetail with appropriate status code
   */
  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<ProblemDetail> onUpstreamError(WebClientResponseException ex) {
    log.warn("Upstream service error: {} - {}", ex.getStatusCode(), ex.getMessage());
    final ProblemDetail pd = ProblemDetail.forStatus(ex.getStatusCode());
    pd.setTitle("Upstream service error");
    pd.setDetail("Error from upstream service: " + ex.getStatusCode() + " - " + ex.getMessage());
    return ResponseEntity.status(ex.getStatusCode()).body(pd);
  }

  /**
   * Handle connection errors to upstream services.
   *
   * @param ex the WebClient exception
   * @return ProblemDetail with 503 status
   */
  @ExceptionHandler(WebClientException.class)
  public ProblemDetail onConnectionError(WebClientException ex) {
    log.error("Failed to connect to upstream service", ex);
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    pd.setTitle("Service unavailable");
    pd.setDetail("Unable to connect to upstream service. Please try again later.");
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
    log.error("Unexpected error in gateway", ex);
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal server error");
    pd.setDetail("An unexpected error occurred in the gateway. Please try again later.");
    return pd;
  }
}
