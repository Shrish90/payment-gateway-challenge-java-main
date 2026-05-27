package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.YearMonth;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;

/**
 * PaymentRequest represents the incoming payment details from the client.
 * It includes validation annotations to ensure data integrity and correctness.
 * @author Shrish Tiwari
 * @version 1.0
 * @since May 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest implements Serializable {
  /**
   * Card number of the payment method.
   * Must be numeric and between 14 and 19 characters.
   */
  @Schema(description = "Card number", example = "4111111111111111")
  @JsonProperty("card_number")
  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "\\d{14,19}", message = "Card number must be numeric and between 14 and 19 characters")
  private String cardNumber;

  /**
   * Expiry month of the card.
   * Must be between 1 and 12.
   */
  @Schema(description = "Expiry month", example = "12")
  @JsonProperty("expiry_month")
  @NotNull(message = "Expiry month is required")
  @Min(value = 1, message = "Expiry month must be between 1 and 12")
  @Max(value = 12, message = "Expiry month must be between 1 and 12")
  private Integer expiryMonth;

  /**
   * Expiry year of the card.
   * Must be a positive integer and the expiry date must be in the future.
   */
  @Schema(description = "Expiry year", example = "2026")
  @JsonProperty("expiry_year")
  @NotNull(message = "Expiry year is required")
  @Min(value = 1900, message = "Expiry year must be a positive integer")
  private Integer expiryYear;

  /**
   * Currency code for the payment.
   * Must be one of USD, EUR, GBP.
   */
  @Schema(description = "Currency code", example = "USD")
  @NotBlank(message = "Currency is required")
  @Pattern(regexp = "^(USD|EUR|GBP)$", message = "Currency must be one of USD, EUR, GBP")
  private String currency;

  /***
   * Amount in minor currency units (e.g., cents).
   * Must be a positive integer.
   */
  @Schema(description = "Amount in minor currency units", example = "100")
  @NotNull(message = "Amount is required")
  @Min(value = 1, message = "Amount must be a positive integer")
  private Integer amount;

  /***
   * CVV code of the card.
   * Must be 3 or 4 numeric characters.
   */
  @Schema(description = "CVV code", example = "123")
  @NotBlank(message = "CVV is required")
  @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 numeric characters")
  private String cvv;

  /***
   * Validates that the expiry date is in the future.
   * This method is ignored during JSON serialization and deserialization.
   * @return true if the expiry date is valid, false otherwise
   */
  @JsonIgnore
  @AssertTrue(message = "Expiry date must be in the future")
  public boolean isExpiryDateValid() {
    if (expiryMonth < 1 || expiryMonth > 12 || expiryYear < 1) {
      return false;
    }
    YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
    return expiry.isAfter(YearMonth.now());
  }

}
