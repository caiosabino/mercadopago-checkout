package com.marketplace.checkout.exception;

public class CheckoutException extends RuntimeException {
    private final int statusCode;

    public CheckoutException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CheckoutException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
