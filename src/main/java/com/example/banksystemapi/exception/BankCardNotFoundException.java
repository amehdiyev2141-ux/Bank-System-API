package com.example.banksystemapi.exception;

public class BankCardNotFoundException extends RuntimeException {

    public BankCardNotFoundException(String message) {
        super(message);
    }
}
