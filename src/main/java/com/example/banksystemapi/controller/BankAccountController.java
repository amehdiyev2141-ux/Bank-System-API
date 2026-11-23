package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.BankAccountRequest;
import com.example.banksystemapi.dto.BankAccountResponse;
import com.example.banksystemapi.service.BankAccountService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
@Tag(name = "Bank Accounts", description = "Create and retrieve customer bank accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @Operation(summary = "Create a bank account")
    public BankAccountResponse createAccount(@Valid @RequestBody BankAccountRequest request) {
        return bankAccountService.createAccount(request);
    }

    @GetMapping
    @Operation(summary = "List bank accounts")
    public List<BankAccountResponse> getAllAccounts() {
        return bankAccountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bank account by ID")
    public BankAccountResponse getAccountById(@PathVariable Long id) {
        return bankAccountService.getAccountById(id);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "List accounts for a customer")
    public List<BankAccountResponse> getAccountsByCustomerId(
            @PathVariable Long customerId) {
        return bankAccountService.getAccountsByCustomerId(customerId);
    }
}
