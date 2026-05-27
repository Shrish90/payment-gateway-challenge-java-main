package com.checkout.payment.gateway.ports;

import com.checkout.payment.gateway.model.PaymentResponse;
import java.util.Optional;
import java.util.UUID;

public interface PaymentsCRUDRepository {

  void add(PaymentResponse paymentResponse);

  Optional<PaymentResponse> get(UUID id);

}
