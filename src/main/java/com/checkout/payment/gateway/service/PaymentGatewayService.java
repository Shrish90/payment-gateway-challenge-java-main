package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.adapters.bank.BankResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.ports.BankClient;
import com.checkout.payment.gateway.repository.PaymentRepository;
import java.util.Set;
import java.util.UUID;
import com.checkout.payment.gateway.utility.PaymentGatewayUtils;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;

@Service
@AllArgsConstructor
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentRepository paymentsRepository;
  private final BankClient bankClient;
  private final Validator validator;

  public PaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PaymentResponse processPayment(PaymentRequest paymentRequest) {

    Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(paymentRequest);
    PaymentResponse paymentResponse;

    if (!violations.isEmpty()) {
      LOG.error("Payment request validation failed with errors {}", violations);
      paymentResponse = createPaymentResponse(paymentRequest, PaymentStatus.REJECTED);
      paymentsRepository.add(paymentResponse);
      return paymentResponse;
    }
    try {
      BankResponse bankResp = bankClient.sendPayment(paymentRequest);

      if(bankResp != null && bankResp.isAuthorized()) {
        paymentResponse = createPaymentResponse(paymentRequest, PaymentStatus.AUTHORIZED);
      } else {
        paymentResponse = createPaymentResponse(paymentRequest, PaymentStatus.DECLINED);
      }
    } catch (BankUnavailableException ex) {
      LOG.error("Bank unavailable while processing payment", ex);
      throw ex;
    }

    paymentsRepository.add(paymentResponse);
    LOG.debug("Processed payment with id {} status {}", paymentResponse.getId(), paymentResponse.getStatus());
    return paymentResponse;
  }

  private PaymentResponse createPaymentResponse(PaymentRequest paymentRequest, PaymentStatus status) {
    return PaymentResponse.builder()
         .id(UUID.randomUUID())
         .status(status)
        .expiryYear(paymentRequest.getExpiryYear())
          .expiryMonth(paymentRequest.getExpiryMonth())
          .cardNumberLastFour(PaymentGatewayUtils.getLastFourCardDigits(paymentRequest.getCardNumber()))
         .amount(paymentRequest.getAmount())
         .currency(paymentRequest.getCurrency())
         .build();
  }
}
