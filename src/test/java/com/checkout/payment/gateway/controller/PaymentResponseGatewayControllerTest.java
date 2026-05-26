package com.checkout.payment.gateway.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.adapters.bank.BankResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.ports.BankClient;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentResponseGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;
  @MockBean
  private BankClient bankClient;

  @Test
  void whenCreatePaymentWithValidRequestThenReturnsCreated() throws Exception {
    String body = "{\"card_number\":\"4111111111111111\",\"expiry_month\":12,\"expiry_year\":2026,\"currency\":\"USD\",\"amount\":100,\"cvv\":123}";
    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(true);
    bankResponse.setAuthorizationCode("auth-code");
    when(bankClient.sendPayment(any())).thenReturn(bankResponse);

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void whenCreatePaymentWithInvalidCardNumberThenReturnsBadRequest() throws Exception {
    String body = "{\"card_number\":\"1234\",\"expiry_month\":12,\"expiry_year\":2026,\"currency\":\"USD\",\"amount\":100,\"cvv\":\"123\"}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Card number must be numeric and between 14 and 19 characters"));
  }

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PaymentResponse paymentResponse = new PaymentResponse();
    paymentResponse.setId(UUID.randomUUID());
    paymentResponse.setAmount(10);
    paymentResponse.setCurrency("USD");
    paymentResponse.setStatus(PaymentStatus.AUTHORIZED);
    paymentResponse.setExpiryMonth(12);
    paymentResponse.setExpiryYear(2024);
    paymentResponse.setCardNumberLastFour(4321);

    paymentsRepository.add(paymentResponse);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + paymentResponse.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(paymentResponse.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(paymentResponse.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(paymentResponse.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(paymentResponse.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(paymentResponse.getCurrency()))
        .andExpect(jsonPath("$.amount").value(paymentResponse.getAmount()));
  }

  @Test
  void whenBankSimulatorIsUnavailableThenReturnsServiceUnavailable() throws Exception {
    when(bankClient.sendPayment(any())).thenThrow(new com.checkout.payment.gateway.exception.BankUnavailableException("Bank unavailable"));

    String body = "{\"card_number\":\"4111111111111111\",\"expiry_month\":12,\"expiry_year\":2026,\"currency\":\"USD\",\"amount\":100,\"cvv\":\"123\"}";
    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.message").value("Bank unavailable"));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }
}
