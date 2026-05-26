package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

public class PostPaymentRequest implements Serializable {

  @Schema(description = "Card number", example = "4111111111111111")
  @JsonProperty("card_number")
  private String cardNumber;

  @Schema(description = "Expiry month", example = "12")
  @JsonProperty("expiry_month")
  private int expiryMonth;

  @Schema(description = "Expiry year", example = "2026")
  @JsonProperty("expiry_year")
  private int expiryYear;

  @Schema(description = "Currency code", example = "USD")
  private String currency;

  @Schema(description = "Amount in minor currency units", example = "100")
  private int amount;

  @Schema(description = "CVV code", example = "123")
  private int cvv;

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  @JsonIgnore
  public int getCardNumberLastFour() {
    if (cardNumber != null && cardNumber.length() >= 4) {
      String lastFour = cardNumber.substring(cardNumber.length() - 4);
      return Integer.parseInt(lastFour);
    }
    return 0;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public int getCvv() {
    return cvv;
  }

  public void setCvv(int cvv) {
    this.cvv = cvv;
  }

  @JsonIgnore
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumber='" + cardNumber + '\'' +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
