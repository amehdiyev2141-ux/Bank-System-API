package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.TransferResponse;
import com.example.banksystemapi.enums.TransferStatus;
import com.example.banksystemapi.exception.TransferNotFoundException;
import com.example.banksystemapi.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Test
    void createTransferReturnsCreatedTransfer() throws Exception {
        when(transferService.createTransfer(any())).thenReturn(response(17L));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountId":1,"destinationAccountId":2,"amount":5.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.sourceAccountId").value(1))
                .andExpect(jsonPath("$.destinationAccountId").value(2))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void createTransferRejectsInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountId":1,"destinationAccountId":2,"amount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
        verifyNoInteractions(transferService);
    }

    @Test
    void createTransferRejectsAmountWithMoreThanTwoDecimalPlaces() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountId":1,"destinationAccountId":2,"amount":1.001}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
        verifyNoInteractions(transferService);
    }

    @Test
    void getTransferByIdReturns404WhenMissing() throws Exception {
        when(transferService.getTransferById(99L))
                .thenThrow(new TransferNotFoundException("Transfer not found with id: 99"));

        mockMvc.perform(get("/api/transfers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transfer not found with id: 99"));
    }

    @Test
    void getAccountTransfersReturnsTransferDtos() throws Exception {
        when(transferService.getAccountTransfers(1L)).thenReturn(List.of(response(17L)));

        mockMvc.perform(get("/api/transfers/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(17))
                .andExpect(jsonPath("$[0].sourceAccountId").value(1));
    }

    private TransferResponse response(Long id) {
        TransferResponse response = new TransferResponse();
        response.setId(id);
        response.setSourceAccountId(1L);
        response.setDestinationAccountId(2L);
        response.setAmount(new BigDecimal("5.00"));
        response.setStatus(TransferStatus.COMPLETED);
        response.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return response;
    }
}
