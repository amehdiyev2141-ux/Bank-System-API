
package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.TransactionRequest;
import com.example.banksystemapi.dto.TransactionResponse;
import com.example.banksystemapi.service.TransactionService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Deposit, withdraw, and account transaction history")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money into an account")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(
            @Valid @RequestBody TransactionRequest request) {

        return transactionService.deposit(request);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money from an account")
    public TransactionResponse withdraw(
            @Valid @RequestBody TransactionRequest request) {

        return transactionService.withdraw(request);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "List transactions for an account")
    public List<TransactionResponse> getAccountTransactions(
            @PathVariable Long accountId) {

        return transactionService.getAccountTransactions(accountId);
    }
}
