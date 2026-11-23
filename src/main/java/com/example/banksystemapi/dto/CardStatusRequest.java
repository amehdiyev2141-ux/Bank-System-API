package com.example.banksystemapi.dto;

import com.example.banksystemapi.enums.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardStatusRequest {

    @NotNull
    private CardStatus status;
}
