package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.InvalidPaymentException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.domain.PostPaymentRequestValidator;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.repository.PaymentRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentRepository paymentsRepository;
  private final com.checkout.payment.gateway.ports.BankClient bankClient;

  public PaymentGatewayService(PaymentRepository paymentsRepository, com.checkout.payment.gateway.ports.BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    PostPaymentRequestValidator.validate(paymentRequest);

    UUID id = UUID.randomUUID();
    PostPaymentResponse response = new PostPaymentResponse();
    response.setId(id);
    response.setCardNumberLastFour(paymentRequest.getCardNumberLastFour());
    response.setExpiryMonth(paymentRequest.getExpiryMonth());
    response.setExpiryYear(paymentRequest.getExpiryYear());
    response.setCurrency(paymentRequest.getCurrency());
    response.setAmount(paymentRequest.getAmount());

    // Call bank simulator to determine authorized/declined
    try {
      com.checkout.payment.gateway.adapters.bank.BankResponse bankResp = bankClient.sendPayment(paymentRequest);
      if (bankResp != null && bankResp.isAuthorized()) {
        response.setStatus(PaymentStatus.AUTHORIZED);
      } else {
        response.setStatus(PaymentStatus.DECLINED);
      }
    } catch (com.checkout.payment.gateway.exception.BankUnavailableException ex) {
      LOG.error("Bank unavailable while processing payment", ex);
      throw ex;
    }

    paymentsRepository.add(response);
    LOG.debug("Processed payment with id {} status {}", id, response.getStatus());
    return response;
  }
}
