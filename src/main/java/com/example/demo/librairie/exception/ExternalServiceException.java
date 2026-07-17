package com.example.demo.librairie.exception;

public class ExternalServiceException extends RuntimeException {

  public ExternalServiceException(String serviceName, String detail) {
    super("Call to external service '" + serviceName + "' failed: " + detail);
  }

  public ExternalServiceException(String serviceName, String detail, Throwable cause) {
    super("Call to external service '" + serviceName + "' failed: " + detail, cause);
  }
}
