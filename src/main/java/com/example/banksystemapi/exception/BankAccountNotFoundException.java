package com.example.banksystemapi.exception;

public class BankAccountNotFoundException extends RuntimeException {

    public BankAccountNotFoundException(String message) {
        super(message);
    }
}
