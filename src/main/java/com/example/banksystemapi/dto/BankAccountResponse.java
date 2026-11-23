
package com.example.banksystemapi.dto;

import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.Currency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BankAccountResponse {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountStatus status;
    private Long customerId;
    private LocalDateTime createdAt;
}
