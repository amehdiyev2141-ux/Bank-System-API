package com.example.banksystemapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    @NotNull
    @Positive
    private Long sourceAccountId;

    @NotNull
    @Positive
    private Long destinationAccountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;
}
