
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
public class TransactionRequest {

    @NotNull
    @Positive
    private Long accountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;
}
