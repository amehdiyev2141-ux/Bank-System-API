package com.example.banksystemapi.dto;

import com.example.banksystemapi.enums.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerStatusRequest {

    @NotNull(message = "Status is required")
    private CustomerStatus status;
}
