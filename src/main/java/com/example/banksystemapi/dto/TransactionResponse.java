
package com.example.banksystemapi.dto;

import com.example.banksystemapi.enums.TransactionStatus;
import com.example.banksystemapi.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {

    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
