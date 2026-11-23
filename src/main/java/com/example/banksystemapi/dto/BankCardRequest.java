package com.example.banksystemapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankCardRequest {

    @NotNull
    @Positive
    private Long bankAccountId;
}
