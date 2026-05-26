package com.checkout.payment.gateway.ports;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.adapters.bank.BankResponse;

public interface BankClient {

  BankResponse sendPayment(PostPaymentRequest request);

}
