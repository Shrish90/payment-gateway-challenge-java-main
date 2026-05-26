package com.checkout.payment.gateway.domain;

import com.checkout.payment.gateway.exception.InvalidPaymentException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.time.YearMonth;
import java.util.Set;

public class PostPaymentRequestValidator {

  private static final Set<String> ALLOWED_CURRENCIES = Set.of("USD", "EUR", "GBP");

  public static void validate(PostPaymentRequest req) {
    if (req == null) {
      throw new InvalidPaymentException("Request body is missing");
    }

    String cardNumber = req.getCardNumber();
    if (cardNumber == null || cardNumber.isBlank()) {
      throw new InvalidPaymentException("Card number is required");
    }
    if (!cardNumber.matches("\\d{14,19}")) {
      throw new InvalidPaymentException("Card number must be numeric and between 14 and 19 characters");
    }

    int month = req.getExpiryMonth();
    int year = req.getExpiryYear();
    if (month < 1 || month > 12) {
      throw new InvalidPaymentException("Expiry month must be between 1 and 12");
    }

    YearMonth expiry = YearMonth.of(year, month);
    YearMonth now = YearMonth.now();
    if (!expiry.isAfter(now)) {
      throw new InvalidPaymentException("Expiry date must be in the future");
    }

    String currency = req.getCurrency();
    if (currency == null || currency.length() != 3 || !ALLOWED_CURRENCIES.contains(currency)) {
      throw new InvalidPaymentException("Currency must be one of: " + ALLOWED_CURRENCIES);
    }

    if (req.getAmount() <= 0) {
      throw new InvalidPaymentException("Amount must be a positive integer");
    }

    String cvv = String.valueOf(req.getCvv());
    if (!cvv.matches("\\d{3,4}")) {
      throw new InvalidPaymentException("CVV must be 3 or 4 numeric characters");
    }
  }
}
