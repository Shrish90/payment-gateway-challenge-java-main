package com.checkout.payment.gateway.repository;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.ports.PaymentsCRUDRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentsRepository implements PaymentsCRUDRepository {

  private final HashMap<UUID, PaymentResponse> payments = new HashMap<>();

  @Override
  public void add(PaymentResponse paymentResponse) {
    payments.put(paymentResponse.getId(), paymentResponse);
  }

  @Override
  public Optional<PaymentResponse> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}
