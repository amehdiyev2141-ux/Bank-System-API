package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.BankCardRequest;
import com.example.banksystemapi.dto.BankCardResponse;
import com.example.banksystemapi.dto.CardStatusRequest;
import com.example.banksystemapi.service.BankCardService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Create cards for active accounts and manage card status")
public class BankCardController {

    private final BankCardService bankCardService;

    @PostMapping
    @Operation(summary = "Create a card for a bank account")
    @ResponseStatus(HttpStatus.CREATED)
    public BankCardResponse createCard(@Valid @RequestBody BankCardRequest request) {
        return bankCardService.createCard(request);
    }

    @GetMapping
    @Operation(summary = "List cards")
    public List<BankCardResponse> getAllCards() {
        return bankCardService.getAllCards();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a card by ID")
    public BankCardResponse getCardById(@PathVariable Long id) {
        return bankCardService.getCardById(id);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "List cards for an account")
    public List<BankCardResponse> getCardsByAccountId(@PathVariable Long accountId) {
        return bankCardService.getCardsByAccountId(accountId);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update card status")
    public BankCardResponse updateCardStatus(
            @PathVariable Long id,
            @Valid @RequestBody CardStatusRequest request) {
        return bankCardService.updateCardStatus(id, request.getStatus());
    }
}
