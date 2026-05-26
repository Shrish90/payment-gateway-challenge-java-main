package com.checkout.payment.gateway.adapters.bank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BankResponse {

  @JsonProperty("authorized")
  private boolean authorized;

  @JsonProperty("authorization_code")
  private String authorizationCode;
}
