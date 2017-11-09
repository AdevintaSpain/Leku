package com.schibstedspain.leku.geocoder.api;

public class NetworkException extends RuntimeException {
  public NetworkException() {
  }

  public NetworkException(String message) {
    super(message);
  }

  public NetworkException(String message, Throwable cause) {
    super(message, cause);
  }

  public NetworkException(Throwable cause) {
    super(cause);
  }
}
