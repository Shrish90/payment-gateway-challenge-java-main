package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application.
 * Handles specific exceptions and returns appropriate HTTP responses.
 * @author Shrish Tiwari
 * @version 1.0
 * @since May 2026
 */
@ControllerAdvice
public class CommonExceptionHandler {

  /** Logger instance for logging error messages when exceptions are handled.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  /** Handles EventProcessingException and returns a 404 Not Found response with the error message.
   * @param ex The EventProcessingException that was thrown.
   * @return A ResponseEntity containing an ErrorResponse with the exception message and an HTTP status code of 404 (Not Found).
   */
  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Event Exception: ", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
        HttpStatus.NOT_FOUND);
  }

  /** Handles BankUnavailableException and returns a 503 Service Unavailable response with a generic error message.
   * @param ex The BankUnavailableException that was thrown.
   * @return A ResponseEntity containing an ErrorResponse with a generic message indicating the bank is unavailable and an HTTP status code of 503 (Service Unavailable).
   */
  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailable(BankUnavailableException ex) {
    LOG.error("Bank unavailable: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Bank unavailable"), HttpStatus.SERVICE_UNAVAILABLE);
  }

  /** Handles any other exceptions that are not specifically handled by other methods and returns a 500 Internal Server Error response with a generic error message.
   * @param ex The Exception that was thrown.
   * @return A ResponseEntity containing an ErrorResponse with a generic message indicating an unexpected error occurred and an HTTP status code of 500 (Internal Server Error).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    LOG.error("Unexpected error: ", ex);
    return new ResponseEntity<>(new ErrorResponse("An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
