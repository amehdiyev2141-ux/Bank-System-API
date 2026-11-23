package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.BankCardResponse;
import com.example.banksystemapi.enums.CardStatus;
import com.example.banksystemapi.exception.BankCardNotFoundException;
import com.example.banksystemapi.service.BankCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankCardController.class)
class BankCardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankCardService bankCardService;

    @Test
    void createCardReturnsMaskedDtoWithoutPanOrCvv() throws Exception {
        when(bankCardService.createCard(any())).thenReturn(response(8L));

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bankAccountId":4}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.maskedCardNumber").value("************3452"))
                .andExpect(jsonPath("$.cardNumber").doesNotExist())
                .andExpect(jsonPath("$.cvv").doesNotExist());
    }

    @Test
    void createCardRejectsMissingAccountId() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bankAccountId").exists());
        verifyNoInteractions(bankCardService);
    }

    @Test
    void getCardsRoutesReturnDtos() throws Exception {
        when(bankCardService.getAllCards()).thenReturn(List.of(response(8L)));
        when(bankCardService.getCardById(8L)).thenReturn(response(8L));
        when(bankCardService.getCardsByAccountId(4L)).thenReturn(List.of(response(8L)));

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].maskedCardNumber").value("************3452"));
        mockMvc.perform(get("/api/cards/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
        mockMvc.perform(get("/api/cards/account/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bankAccountId").value(4));
    }

    @Test
    void getCardByIdReturns404WhenMissing() throws Exception {
        when(bankCardService.getCardById(99L))
                .thenThrow(new BankCardNotFoundException("Card not found with id: 99"));

        mockMvc.perform(get("/api/cards/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found with id: 99"));
    }

    @Test
    void patchCardStatusUpdatesStatus() throws Exception {
        when(bankCardService.updateCardStatus(8L, CardStatus.BLOCKED)).thenReturn(response(8L,
                CardStatus.BLOCKED));

        mockMvc.perform(patch("/api/cards/8/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOCKED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void patchCardStatusRejectsNullStatus() throws Exception {
        mockMvc.perform(patch("/api/cards/8/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
        verifyNoInteractions(bankCardService);
    }

    private BankCardResponse response(Long id) {
        return response(id, CardStatus.ACTIVE);
    }

    private BankCardResponse response(Long id, CardStatus status) {
        BankCardResponse response = new BankCardResponse();
        response.setId(id);
        response.setMaskedCardNumber("************3452");
        response.setStatus(status);
        response.setBankAccountId(4L);
        response.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return response;
    }
}
