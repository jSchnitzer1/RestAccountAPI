package com.accounts.api.exception;

public class DatabaseFailureException extends RuntimeException {
    private static final long SerialVersionUID = 10L;

    public DatabaseFailureException(String message) {
        super(message);
    }
}
