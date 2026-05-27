package com.checkout.payment.gateway.ports;

import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.adapters.bank.BankResponse;

/** * Port interface for communicating with the bank's payment processing system.
 * This interface defines the contract for sending payment requests to the bank
 * and receiving responses. It abstracts away the details of how the communication
 * with the bank is implemented, allowing for flexibility in choosing different
 * implementations (e.g., REST API, SOAP, etc.) without affecting the rest of the
 * application.
 * @author Shrish Tiwari
 * @version 1.0
 * @since May 2026
 */
public interface BankClient {

  /** Sends a payment request to the bank and returns the response.
   * @param request The PaymentRequest object containing the details of the payment to be processed.
   * @return A BankResponse object containing the result of the payment processing, including whether it was authorized and any relevant authorization codes.
   */
  BankResponse sendPayment(PaymentRequest request);

}
