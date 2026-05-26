package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** PaymentGatewayController is a REST controller that handles HTTP requests related to payment processing.
 * It provides endpoints for retrieving payment details by ID and for creating new payment requests.
 * The controller interacts with the PaymentGatewayService to perform the necessary business logic and returns appropriate HTTP responses.
 * @author Shrish Tiwari
 * @version 1.0
 * @since May 2026
 */
@RestController
@AllArgsConstructor
public class PaymentGatewayController {

  /** The PaymentGatewayService is injected into the controller to handle the business logic of processing payments and retrieving payment details.
   */
  private final PaymentGatewayService paymentGatewayService;

  /** Retrieves the payment details for a given payment ID.
   * @param id The UUID of the payment to retrieve.
   * @return A ResponseEntity containing the PaymentResponse and an HTTP status code of 200 (OK).
   */
  @GetMapping("/payment/{id}")
  public ResponseEntity<PaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  /** Creates a new payment request and processes it.
   * @param request The PaymentRequest object containing the details of the payment to be processed. It is validated for correctness.
   * @return A ResponseEntity containing the PaymentResponse and an HTTP status code of 201 (Created).
   */
  @PostMapping("/payment")
  public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
    PaymentResponse response = paymentGatewayService.processPayment(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}
