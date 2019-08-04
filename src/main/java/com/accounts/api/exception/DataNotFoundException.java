package com.accounts.api.exception;

public class DataNotFoundException extends RuntimeException {
    private static final long SerialVersionUID = 10L;

    public DataNotFoundException(String message) {
        super(message);
    }
}
