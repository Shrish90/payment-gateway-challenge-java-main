package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** PaymentResponse represents the response returned after processing a payment request.
 * It contains details about the payment status, card information, amount, currency, and any violations that occurred during processing.
 * @author Shrish Tiwari
 * @version 1.0
 * @since May 2026
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
  /** The unique identifier for the payment transaction. */
  private UUID id;

  /** The status of the payment transaction */
  private PaymentStatus status;

  /** The last four digits of the card number used for the payment. */
  private Integer cardNumberLastFour;

  /** The month of the card's expiry date. */
  private Integer expiryMonth;

  /** The year of the card's expiry date. */
  private Integer expiryYear;

  /** The currency in which the payment was made. */
  private String currency;

  /** The amount of the payment transaction. */
  private Integer amount;

  /** A list of any violations that occurred during the payment processing. */
  private List<String> violations;
}
