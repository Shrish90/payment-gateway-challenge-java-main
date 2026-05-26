package com.checkout.payment.gateway.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.adapters.bank.BankResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.ports.BankClient;
import com.checkout.payment.gateway.repository.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PaymentResponseGatewayServiceTest {

  @Test
  void processPaymentSetsAuthorizedStatusWhenBankRespondsAuthorized() {
    PaymentRepository repository = Mockito.mock(PaymentRepository.class);
    BankClient bankClient = Mockito.mock(BankClient.class);
    PaymentGatewayService service = new PaymentGatewayService(repository, bankClient);

    PaymentRequest request = new PaymentRequest();
    request.setCardNumber("4111111111111111");
    request.setExpiryMonth(12);
    request.setExpiryYear(2026);
    request.setCurrency("USD");
    request.setAmount(100);
    request.setCvv("123");

    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(true);
    bankResponse.setAuthorizationCode("auth-code");
    when(bankClient.sendPayment(request)).thenReturn(bankResponse);

    PaymentResponse response = service.processPayment(request);

    Assertions.assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
    Assertions.assertEquals("USD", response.getCurrency());
    Assertions.assertEquals(100, response.getAmount());
    Assertions.assertNotNull(response.getId());
    verify(repository).add(response);
  }

  @Test
  void processPaymentSetsDeclinedStatusWhenBankRespondsNotAuthorized() {
    PaymentRepository repository = Mockito.mock(PaymentRepository.class);
    BankClient bankClient = Mockito.mock(BankClient.class);
    PaymentGatewayService service = new PaymentGatewayService(repository, bankClient);

    PaymentRequest request = new PaymentRequest();
    request.setCardNumber("4111111111111112");
    request.setExpiryMonth(12);
    request.setExpiryYear(2026);
    request.setCurrency("USD");
    request.setAmount(100);
    request.setCvv("123");

    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(false);
    bankResponse.setAuthorizationCode("decline-code");
    when(bankClient.sendPayment(request)).thenReturn(bankResponse);

    PaymentResponse response = service.processPayment(request);

    Assertions.assertEquals(PaymentStatus.DECLINED, response.getStatus());
    verify(repository).add(response);
  }
}
