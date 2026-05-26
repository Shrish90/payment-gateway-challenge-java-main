package com.checkout.payment.gateway.utility;

public class PaymentGatewayUtils {

  public static int getLastFourCardDigits(String cardNumber){
    if (cardNumber == null || cardNumber.length() < 4) {
      throw new IllegalArgumentException("Card number must be at least 4 digits long");
    }
    return Integer.parseInt(cardNumber.substring(cardNumber.length() - 4));
  }

}
