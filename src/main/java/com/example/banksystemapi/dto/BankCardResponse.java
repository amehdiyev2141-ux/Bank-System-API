package com.example.banksystemapi.dto;

import com.example.banksystemapi.enums.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BankCardResponse {

    private Long id;
    private String maskedCardNumber;
    private CardStatus status;
    private Long bankAccountId;
    private LocalDateTime createdAt;
}
