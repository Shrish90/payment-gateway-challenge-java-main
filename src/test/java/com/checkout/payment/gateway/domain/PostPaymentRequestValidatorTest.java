package com.checkout.payment.gateway.domain;

import com.checkout.payment.gateway.exception.InvalidPaymentException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PostPaymentRequestValidatorTest {

  @Test
  void validRequestPassesValidation() {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("4111111111111111");
    request.setExpiryMonth(12);
    request.setExpiryYear(2026);
    request.setCurrency("USD");
    request.setAmount(100);
    request.setCvv(123);

    Assertions.assertDoesNotThrow(() -> PostPaymentRequestValidator.validate(request));
  }

  @Test
  void invalidCardNumberThrowsInvalidPaymentException() {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("1234");
    request.setExpiryMonth(12);
    request.setExpiryYear(2026);
    request.setCurrency("USD");
    request.setAmount(100);
    request.setCvv(123);

    InvalidPaymentException ex = Assertions.assertThrows(InvalidPaymentException.class,
        () -> PostPaymentRequestValidator.validate(request));
    Assertions.assertEquals("Card number must be numeric and between 14 and 19 characters", ex.getMessage());
  }

  @Test
  void expiredCardThrowsInvalidPaymentException() {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("4111111111111111");
    request.setExpiryMonth(1);
    request.setExpiryYear(2020);
    request.setCurrency("USD");
    request.setAmount(100);
    request.setCvv(123);

    InvalidPaymentException ex = Assertions.assertThrows(InvalidPaymentException.class,
        () -> PostPaymentRequestValidator.validate(request));
    Assertions.assertEquals("Expiry date must be in the future", ex.getMessage());
  }
}
