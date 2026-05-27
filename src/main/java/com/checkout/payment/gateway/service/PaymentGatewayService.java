package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.adapters.bank.BankResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.ports.BankClient;
import com.checkout.payment.gateway.repository.PaymentsCRUDRepository;
import com.checkout.payment.gateway.utility.PaymentGatewayUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for processing payment requests and managing payment records.
 * It validates incoming payment requests, interacts with the bank client to process payments,
 * and stores the results in the payments repository.
 * @author Shrish Tiwari
 * @version 1.0
 * @since May 2026
 */
@Service
@AllArgsConstructor
public class PaymentGatewayService {

  /** Logger instance for logging information and errors during payment processing.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  /** The PaymentsCRUDRepository is used to store and retrieve payment responses based on their unique identifiers.
   */
  private final PaymentsCRUDRepository paymentsRepository;

  /** The BankClient is an interface that defines the methods for interacting with the bank to process payments.
   * It abstracts the details of the bank communication and allows for different implementations.
   */
  private final BankClient bankClient;

  /** The Validator is used to validate the incoming PaymentRequest objects against defined constraints to ensure data integrity before processing.
   */
  private final Validator validator;

  /** Retrieves a payment response by its unique identifier.
   * @param id The UUID of the payment to retrieve.
   * @return The PaymentResponse associated with the given ID.
   * @throws EventProcessingException if no payment is found for the provided ID.
   */
  public PaymentResponse getPaymentById(UUID id) {
    LOG.info("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid Payment ID"));
  }

  /** Processes a payment request by validating the input, interacting with the bank client, and storing the result.
   * @param paymentRequest The PaymentRequest object containing the details of the payment to be processed.
   * @return A PaymentResponse object containing the result of the payment processing.
   * @throws BankUnavailableException if the bank is unavailable during payment processing.
   */
  public PaymentResponse processPayment(PaymentRequest paymentRequest) {
    LOG.info("Processing payment request for amount {} and currency {}", paymentRequest.getAmount(), paymentRequest.getCurrency());

    Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(paymentRequest);
    PaymentResponse paymentResponse;

    if (!violations.isEmpty()) {
      LOG.error("Payment request validation failed with errors {}", violations);
      // TODO: Update paymentRequest to request object in case reject status should be saved into database
      paymentResponse = createPaymentResponse(new PaymentRequest(), PaymentStatus.REJECTED,
          violations.stream()
              .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
              .toList());
      // TODO: Need to Clarify if rejected payment request should be saved into database with reject status or not. If yes, then uncomment the below line to save the rejected payment response into database.
      //paymentsRepository.add(paymentResponse);
      return paymentResponse;
    }
    try {
      BankResponse bankResponse = bankClient.sendPayment(paymentRequest);

      if (bankResponse != null && bankResponse.isAuthorized()) {
        paymentResponse = createPaymentResponse(paymentRequest, PaymentStatus.AUTHORIZED, null);
      } else {
        paymentResponse = createPaymentResponse(paymentRequest, PaymentStatus.DECLINED, null);
      }
    } catch (BankUnavailableException ex) {
      LOG.error("Bank unavailable while processing payment", ex);
      throw ex;
    }

    paymentsRepository.add(paymentResponse);
    LOG.debug("Processed payment with id {} status {}", paymentResponse.getId(),
        paymentResponse.getStatus());
    return paymentResponse;
  }

  /** Helper method to create a PaymentResponse object based on the payment request, status, and any validation violations.
   * @param paymentRequest The original PaymentRequest object containing the details of the payment.
   * @param status The PaymentStatus to be set in the response (AUTHORIZED, DECLINED, or REJECTED).
   * @param violations A list of violation messages if the payment request was rejected due to validation errors.
   * @return A PaymentResponse object populated with the provided information.
   */
  private PaymentResponse createPaymentResponse(PaymentRequest paymentRequest,
      PaymentStatus status, List<String> violations) {
    return PaymentResponse.builder()
        .id(paymentRequest.getAmount() != null ? UUID.randomUUID() : null) // TODO: update based on reject save in database.
        .status(status)
        .expiryYear(paymentRequest.getExpiryYear())
        .expiryMonth(paymentRequest.getExpiryMonth())
        .cardNumberLastFour(getCardNumberLastFourSafe(paymentRequest.getCardNumber()))
        .amount(paymentRequest.getAmount())
        .currency(paymentRequest.getCurrency())
        .violations(violations)
        .build();
  }
  /** Helper method to safely extract the last four digits of a card number, handling potential exceptions.
   * @param cardNumber The full card number as a String.
   * @return An integer representing the last four digits of the card number, or the entire card number if it cannot be parsed.
   */
  private Integer getCardNumberLastFourSafe(String cardNumber) {
    try {
      return PaymentGatewayUtils.getLastFourCardDigits(cardNumber);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
