package com.hetero.exception;

public class TransactionNotUpdateException extends RuntimeException {
    public TransactionNotUpdateException (String message) {
        super(message);
    }
}
