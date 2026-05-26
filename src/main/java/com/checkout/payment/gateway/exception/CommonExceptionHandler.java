package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Page not found"),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InvalidPaymentException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidPaymentException ex) {
    LOG.warn("Invalid payment request: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(com.checkout.payment.gateway.exception.BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailable(com.checkout.payment.gateway.exception.BankUnavailableException ex) {
    LOG.error("Bank unavailable: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Bank unavailable"), HttpStatus.SERVICE_UNAVAILABLE);
  }
}
