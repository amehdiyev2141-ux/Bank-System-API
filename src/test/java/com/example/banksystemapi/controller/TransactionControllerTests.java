package com.example.banksystemapi.controller;

import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void depositReturnsNotFoundForUnknownAccount() throws Exception {
        when(transactionService.deposit(any()))
                .thenThrow(new BankAccountNotFoundException("Bank account not found with id: 99"));

        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":99,"amount":5.00}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank account not found with id: 99"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void depositRejectsZeroAmountBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"amount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());

        verifyNoInteractions(transactionService);
    }

    @Test
    void withdrawRejectsNegativeAmountBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"amount":-1.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());

        verifyNoInteractions(transactionService);
    }

    @Test
    void depositRejectsAmountWithMoreThanTwoDecimalPlaces() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"amount":1.001}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());

        verifyNoInteractions(transactionService);
    }

    @Test
    void depositRejectsNonPositiveAccountIdBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":-1,"amount":1.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.accountId").exists());

        verifyNoInteractions(transactionService);
    }

    @Test
    void withdrawReturnsBadRequestWhenBusinessRuleRejectsTransaction() throws Exception {
        when(transactionService.withdraw(any()))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        mockMvc.perform(post("/api/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"amount":25.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }
}
