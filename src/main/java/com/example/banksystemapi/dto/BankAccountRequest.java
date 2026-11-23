
package com.example.banksystemapi.dto;

import com.example.banksystemapi.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private Currency currency;

}
