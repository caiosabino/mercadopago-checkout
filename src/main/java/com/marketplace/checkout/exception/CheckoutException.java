package com.marketplace.checkout.exception;

import java.util.List;

public class CheckoutException extends RuntimeException {
    private final int statusCode;
    private final List<String> details;

    public CheckoutException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.details = null;
    }

    public CheckoutException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.details = null;
    }

    public CheckoutException(String message, int statusCode, Throwable cause, List<String> details) {
        super(message, cause);
        this.statusCode = statusCode;
        this.details = details;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<String> getDetails() {
        return details;
    }
}
