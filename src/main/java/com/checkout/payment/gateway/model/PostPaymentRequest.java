package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPaymentRequest implements Serializable {

  @Schema(description = "Card number", example = "4111111111111111")
  @JsonProperty("card_number")
  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "\\d{14,19}", message = "Card number must be numeric and between 14 and 19 characters")
  private String cardNumber;

  @Schema(description = "Expiry month", example = "12")
  @JsonProperty("expiry_month")
  @Min(value = 1, message = "Expiry month must be between 1 and 12")
  @Max(value = 12, message = "Expiry month must be between 1 and 12")
  private int expiryMonth;

  @Schema(description = "Expiry year", example = "2026")
  @JsonProperty("expiry_year")
  @Min(value = 1, message = "Expiry year must be a positive integer")
  private int expiryYear;

  @Schema(description = "Currency code", example = "USD")
  @NotBlank(message = "Currency is required")
  @Pattern(regexp = "^(USD|EUR|GBP)$", message = "Currency must be one of USD, EUR, GBP")
  private String currency;

  @Schema(description = "Amount in minor currency units", example = "100")
  @Min(value = 1, message = "Amount must be a positive integer")
  private int amount;

  @Schema(description = "CVV code", example = "123")
  @NotBlank(message = "CVV is required")
  @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 numeric characters")
  private String cvv;

  @JsonIgnore
  public int getCardNumberLastFour() {
    if (cardNumber != null && cardNumber.length() >= 4) {
      String lastFour = cardNumber.substring(cardNumber.length() - 4);
      return Integer.parseInt(lastFour);
    }
    return 0;
  }

  @JsonIgnore
  @AssertTrue(message = "Expiry date must be in the future")
  public boolean isExpiryDateValid() {
    if (expiryMonth < 1 || expiryMonth > 12 || expiryYear < 1) {
      return false;
    }
    YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
    return expiry.isAfter(YearMonth.now());
  }

  @JsonIgnore
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }
}
