package com.checkout.payment.gateway.adapters.bank;

import com.checkout.payment.gateway.adapters.bank.BankPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.ports.BankClient;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpBankClient implements BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(HttpBankClient.class);

  private final RestTemplate rest = new RestTemplate();
  private final String endpoint = "http://localhost:8080/payments";
  private static final int MAX_RETRIES = 2;

  @Override
  public BankResponse sendPayment(PostPaymentRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    BankPaymentRequest bankRequest = new BankPaymentRequest(
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCurrency(),
        request.getAmount(),
        String.valueOf(request.getCvv())
    );
    HttpEntity<BankPaymentRequest> entity = new HttpEntity<>(bankRequest, headers);

    int attempt = 0;
    while (true) {
      attempt++;
      try {
        BankResponse resp = rest.postForObject(endpoint, entity, BankResponse.class);
        return resp != null ? resp : new BankResponse();
      } catch (HttpServerErrorException ex) {
        LOG.error("Bank simulator returned server error on attempt {}: {}", attempt, ex.getStatusCode());
        if (attempt >= MAX_RETRIES) {
          throw new BankUnavailableException("Bank simulator unavailable");
        }
      } catch (RestClientException ex) {
        LOG.error("Error calling bank simulator on attempt {}", attempt, ex);
        if (attempt >= MAX_RETRIES) {
          throw new BankUnavailableException("Bank simulator unavailable");
        }
      }
    }
  }
}
