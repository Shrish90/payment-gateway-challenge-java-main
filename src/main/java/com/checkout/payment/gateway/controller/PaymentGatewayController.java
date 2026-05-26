package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @GetMapping("/payment/{id}")
  public ResponseEntity<PostPaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @PostMapping("/payment")
  public ResponseEntity<PostPaymentResponse> createPayment(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Payment request payload",
          required = true,
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(value = "{\"card_number\":\"4111111111111111\",\"expiry_month\":12,\"expiry_year\":2026,\"currency\":\"USD\",\"amount\":100,\"cvv\":\"123\"}")
          )
      )
      @Valid
      @RequestBody PostPaymentRequest request) {
    PostPaymentResponse response = paymentGatewayService.processPayment(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}
