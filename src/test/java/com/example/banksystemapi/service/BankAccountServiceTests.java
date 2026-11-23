package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.BankAccountRequest;
import com.example.banksystemapi.dto.BankAccountResponse;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.Customer;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.Currency;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.exception.CustomerNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BankAccountServiceTests {

    private final BankAccountRepository bankAccountRepository = mock(BankAccountRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final BankAccountService bankAccountService =
            new BankAccountService(bankAccountRepository, customerRepository);

    @Test
    void createAccountSetsDefaultsAndMapsSavedAccountToResponse() {
        Customer customer = customer(12L);
        LocalDateTime createdAt = LocalDateTime.parse("2026-01-01T10:00:00");
        when(customerRepository.findById(12L)).thenReturn(Optional.of(customer));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            BankAccount account = invocation.getArgument(0);
            account.setId(34L);
            account.setCreatedAt(createdAt);
            return account;
        });

        BankAccountResponse response = bankAccountService.createAccount(request(12L, Currency.AZN));

        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        BankAccount savedAccount = accountCaptor.getValue();
        assertTrue(savedAccount.getAccountNumber().matches("[A-F0-9]{16}"));
        assertEquals(new BigDecimal("0.00"), savedAccount.getBalance());
        assertEquals(Currency.AZN, savedAccount.getCurrency());
        assertEquals(AccountStatus.ACTIVE, savedAccount.getStatus());
        assertEquals(customer, savedAccount.getCustomer());

        assertEquals(34L, response.getId());
        assertEquals(savedAccount.getAccountNumber(), response.getAccountNumber());
        assertEquals(new BigDecimal("0.00"), response.getBalance());
        assertEquals(Currency.AZN, response.getCurrency());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(12L, response.getCustomerId());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void createAccountRetriesWhenGeneratedAccountNumberAlreadyExists() {
        when(customerRepository.findById(12L)).thenReturn(Optional.of(customer(12L)));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(true, false);
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bankAccountService.createAccount(request(12L, Currency.USD));

        ArgumentCaptor<String> accountNumberCaptor = ArgumentCaptor.forClass(String.class);
        verify(bankAccountRepository, times(2))
                .existsByAccountNumber(accountNumberCaptor.capture());
        List<String> generatedNumbers = accountNumberCaptor.getAllValues();
        assertNotEquals(generatedNumbers.get(0), generatedNumbers.get(1));
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        assertEquals(generatedNumbers.get(1), accountCaptor.getValue().getAccountNumber());
    }

    @Test
    void createAccountFailsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> bankAccountService.createAccount(request(99L, Currency.EUR)));

        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void getAllAccountsMapsEntitiesToResponses() {
        when(bankAccountRepository.findAll()).thenReturn(List.of(account(34L, 12L)));

        List<BankAccountResponse> responses = bankAccountService.getAllAccounts();

        assertEquals(1, responses.size());
        assertEquals(34L, responses.getFirst().getId());
        assertEquals(12L, responses.getFirst().getCustomerId());
    }

    @Test
    void getAccountByIdMapsEntityToResponse() {
        when(bankAccountRepository.findById(34L)).thenReturn(Optional.of(account(34L, 12L)));

        BankAccountResponse response = bankAccountService.getAccountById(34L);

        assertEquals(34L, response.getId());
        assertEquals(12L, response.getCustomerId());
    }

    @Test
    void getAccountByIdThrowsWhenAccountDoesNotExist() {
        when(bankAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.getAccountById(99L));
    }

    @Test
    void getAccountsByCustomerIdMapsEntitiesToResponses() {
        when(bankAccountRepository.findAllByCustomer_Id(12L))
                .thenReturn(List.of(account(34L, 12L)));

        List<BankAccountResponse> responses = bankAccountService.getAccountsByCustomerId(12L);

        assertEquals(1, responses.size());
        assertEquals(34L, responses.getFirst().getId());
        assertEquals(12L, responses.getFirst().getCustomerId());
    }

    private Customer customer(Long id) {
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    private BankAccount account(Long id, Long customerId) {
        BankAccount account = new BankAccount();
        account.setId(id);
        account.setAccountNumber("A1B2C3D4E5F60718");
        account.setBalance(new BigDecimal("0.00"));
        account.setCurrency(Currency.AZN);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer(customerId));
        account.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return account;
    }

    private BankAccountRequest request(Long customerId, Currency currency) {
        BankAccountRequest request = new BankAccountRequest();
        request.setCustomerId(customerId);
        request.setCurrency(currency);
        return request;
    }
}
