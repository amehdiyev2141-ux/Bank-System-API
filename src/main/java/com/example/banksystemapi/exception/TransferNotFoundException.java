package com.example.banksystemapi.exception;

public class TransferNotFoundException extends RuntimeException {

    public TransferNotFoundException(String message) {
        super(message);
    }
}
