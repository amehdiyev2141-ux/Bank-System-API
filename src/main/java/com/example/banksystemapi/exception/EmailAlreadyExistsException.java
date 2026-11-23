package com.example.banksystemapi.exception;

public class EmailAlreadyExistsException extends RuntimeException {


    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
