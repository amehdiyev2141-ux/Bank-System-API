package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.TransactionRequest;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionServiceTests {

    @Test
    void withdrawRejectsInsufficientBalanceWithoutChangingBalance() {
        Fixture fixture = fixture(AccountStatus.ACTIVE, "10.00");

        assertThrows(IllegalArgumentException.class,
                () -> fixture.service.withdraw(request("10.01")));

        assertEquals(new BigDecimal("10.00"), fixture.account.getBalance());
        verify(fixture.bankAccountRepository, never()).save(any(BankAccount.class));
        verifyNoInteractions(fixture.transactionRepository);
    }

    @Test
    void depositAndWithdrawRejectBlockedAndClosedAccountsWithoutChangingBalance() {
        for (AccountStatus status : new AccountStatus[]{AccountStatus.BLOCKED, AccountStatus.CLOSED}) {
            Fixture fixture = fixture(status, "10.00");

            assertThrows(IllegalArgumentException.class,
                    () -> fixture.service.deposit(request("1.00")));
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.service.withdraw(request("1.00")));

            assertEquals(new BigDecimal("10.00"), fixture.account.getBalance());
            verify(fixture.bankAccountRepository, never()).save(any(BankAccount.class));
            verifyNoInteractions(fixture.transactionRepository);
        }
    }

    @Test
    void depositAndWithdrawRejectZeroAndNegativeAmountsWithoutChangingBalance() {
        for (String amount : new String[]{"0.00", "-1.00"}) {
            Fixture fixture = fixture(AccountStatus.ACTIVE, "10.00");

            assertThrows(IllegalArgumentException.class,
                    () -> fixture.service.deposit(request(amount)));
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.service.withdraw(request(amount)));

            assertEquals(new BigDecimal("10.00"), fixture.account.getBalance());
            verifyNoInteractions(fixture.bankAccountRepository, fixture.transactionRepository);
        }
    }

    @Test
    void depositAndWithdrawUseNotFoundExceptionForMissingAccount() {
        Fixture fixture = fixtureWithoutAccount();

        assertThrows(BankAccountNotFoundException.class,
                () -> fixture.service.deposit(request("1.00")));
        assertThrows(BankAccountNotFoundException.class,
                () -> fixture.service.withdraw(request("1.00")));

        verifyNoInteractions(fixture.transactionRepository);
    }

    private Fixture fixture(AccountStatus status, String balance) {
        BankAccountRepository bankAccountRepository = mock(BankAccountRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setBalance(new BigDecimal(balance));
        account.setStatus(status);
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        return new Fixture(new TransactionService(transactionRepository, bankAccountRepository),
                bankAccountRepository, transactionRepository, account);
    }

    private Fixture fixtureWithoutAccount() {
        BankAccountRepository bankAccountRepository = mock(BankAccountRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        return new Fixture(new TransactionService(transactionRepository, bankAccountRepository),
                bankAccountRepository, transactionRepository, null);
    }

    private TransactionRequest request(String amount) {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal(amount));
        return request;
    }

    private record Fixture(
            TransactionService service,
            BankAccountRepository bankAccountRepository,
            TransactionRepository transactionRepository,
            BankAccount account) {
    }
}
