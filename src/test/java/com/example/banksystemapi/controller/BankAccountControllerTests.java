package com.example.banksystemapi.controller;

import com.example.banksystemapi.dto.BankAccountRequest;
import com.example.banksystemapi.dto.BankAccountResponse;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.Currency;
import com.example.banksystemapi.service.BankAccountService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAccountController.class)
class BankAccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankAccountService bankAccountService;

    @Test
    void createAccountReturnsAccountResponse() throws Exception {
        BankAccountResponse response = new BankAccountResponse();
        response.setId(25L);
        response.setAccountNumber("A1B2C3D4E5F60718");
        response.setBalance(new BigDecimal("0.00"));
        response.setCurrency(Currency.AZN);
        response.setStatus(AccountStatus.ACTIVE);
        response.setCustomerId(12L);
        response.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        when(bankAccountService.createAccount(any(BankAccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":12,"currency":"AZN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(25))
                .andExpect(jsonPath("$.accountNumber").value("A1B2C3D4E5F60718"))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.currency").value("AZN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.customerId").value(12));
    }

    @Test
    void createAccountRejectsMissingRequiredRequestFields() throws Exception {
        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.customerId").exists())
                .andExpect(jsonPath("$.errors.currency").exists());
    }

    @Test
    void getAllAccountsReturnsAccountResponses() throws Exception {
        when(bankAccountService.getAllAccounts()).thenReturn(List.of(response(25L, 12L)));

        mockMvc.perform(get("/api/bank-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(25))
                .andExpect(jsonPath("$[0].customerId").value(12))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getAccountByIdReturnsAccountResponse() throws Exception {
        when(bankAccountService.getAccountById(25L)).thenReturn(response(25L, 12L));

        mockMvc.perform(get("/api/bank-accounts/25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(25))
                .andExpect(jsonPath("$.customerId").value(12));
    }

    @Test
    void getAccountByIdReturnsNotFound() throws Exception {
        when(bankAccountService.getAccountById(99L))
                .thenThrow(new com.example.banksystemapi.exception.BankAccountNotFoundException(
                        "Bank account not found with id: 99"));

        mockMvc.perform(get("/api/bank-accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank account not found with id: 99"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void getAccountsByCustomerIdReturnsAccountResponses() throws Exception {
        when(bankAccountService.getAccountsByCustomerId(12L))
                .thenReturn(List.of(response(25L, 12L)));

        mockMvc.perform(get("/api/bank-accounts/customer/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(25))
                .andExpect(jsonPath("$[0].customerId").value(12));
    }

    private BankAccountResponse response(Long id, Long customerId) {
        BankAccountResponse response = new BankAccountResponse();
        response.setId(id);
        response.setAccountNumber("A1B2C3D4E5F60718");
        response.setBalance(new BigDecimal("0.00"));
        response.setCurrency(Currency.AZN);
        response.setStatus(AccountStatus.ACTIVE);
        response.setCustomerId(customerId);
        response.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return response;
    }
}
