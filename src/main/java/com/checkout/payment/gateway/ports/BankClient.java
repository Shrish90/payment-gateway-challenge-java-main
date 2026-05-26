package com.checkout.payment.gateway.ports;

import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.adapters.bank.BankResponse;

public interface BankClient {

  BankResponse sendPayment(PaymentRequest request);

}
