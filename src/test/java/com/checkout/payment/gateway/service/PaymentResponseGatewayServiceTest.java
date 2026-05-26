package com.checkout.payment.gateway.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.adapters.bank.BankResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.ports.BankClient;
import com.checkout.payment.gateway.repository.PaymentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;

class PaymentResponseGatewayServiceTest {

  @Test
  void processPaymentSetsAuthorizedStatusWhenBankRespondsAuthorized() {
    PaymentRepository repository = Mockito.mock(PaymentRepository.class);
    BankClient bankClient = Mockito.mock(BankClient.class);
    Validator validator = Mockito.mock(Validator.class);
    when(validator.validate(any(PaymentRequest.class))).thenReturn(Collections.emptySet());
    PaymentGatewayService service = new PaymentGatewayService(repository, bankClient, validator);

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
    Validator validator = Mockito.mock(Validator.class);
    when(validator.validate(any(PaymentRequest.class))).thenReturn(Collections.emptySet());
    PaymentGatewayService service = new PaymentGatewayService(repository, bankClient, validator);

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

  @Test
  void processPaymentAddsViolationsWhenRequestIsRejected() {
    PaymentRepository repository = Mockito.mock(PaymentRepository.class);
    BankClient bankClient = Mockito.mock(BankClient.class);
    Validator validator = Mockito.mock(Validator.class);
    ConstraintViolation<PaymentRequest> violation = Mockito.mock(ConstraintViolation.class);
    Path propertyPath = Mockito.mock(Path.class);
    Mockito.when(propertyPath.toString()).thenReturn("cardNumber");
    Mockito.when(violation.getPropertyPath()).thenReturn(propertyPath);
    Mockito.when(violation.getMessage()).thenReturn("Card number must be numeric and between 14 and 19 characters");
    Mockito.when(validator.validate(any(PaymentRequest.class))).thenReturn(Collections.singleton(violation));
    PaymentGatewayService service = new PaymentGatewayService(repository, bankClient, validator);

    PaymentRequest request = new PaymentRequest();
    request.setCardNumber("1234");
    request.setExpiryMonth(12);
    request.setExpiryYear(2026);
    request.setCurrency("USD");
    request.setAmount(100);
    request.setCvv("123");

    var response = service.processPayment(request);

    Assertions.assertEquals(PaymentStatus.REJECTED, response.getStatus());
    Assertions.assertNotNull(response.getViolations());
    Assertions.assertEquals(1, response.getViolations().size());
    Assertions.assertEquals("cardNumber: Card number must be numeric and between 14 and 19 characters",
        response.getViolations().get(0));
    verify(bankClient, never()).sendPayment(any());
    verify(repository).add(response);
  }
}
