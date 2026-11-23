package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.TransferRequest;
import com.example.banksystemapi.dto.TransferResponse;
import com.example.banksystemapi.service.TransferService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Transfer funds between active bank accounts and view transfer history")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Transfer money between accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse createTransfer(@Valid @RequestBody TransferRequest request) {
        return transferService.createTransfer(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transfer by ID")
    public TransferResponse getTransferById(@PathVariable Long id) {
        return transferService.getTransferById(id);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "List transfers involving an account")
    public List<TransferResponse> getAccountTransfers(@PathVariable Long accountId) {
        return transferService.getAccountTransfers(accountId);
    }
}
