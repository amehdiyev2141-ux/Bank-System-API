package com.example.banksystemapi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTransferNotFound(
            TransferNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(BankCardNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBankCardNotFound(
            BankCardNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(BankAccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBankAccountNotFound(
            BankAccountNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(
            CustomerNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation() {
        return error(HttpStatus.CONFLICT,
                "Data conflicts with an existing record", Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        return error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status, String message, Map<String, String> errors) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("errors", errors);
        return ResponseEntity
                .status(status)
                .body(response);
    }
}
