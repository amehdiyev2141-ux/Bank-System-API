package com.example.banksystemapi.dto;

import com.example.banksystemapi.enums.TransferStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransferResponse {

    private Long id;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private TransferStatus status;
    private LocalDateTime createdAt;
}
