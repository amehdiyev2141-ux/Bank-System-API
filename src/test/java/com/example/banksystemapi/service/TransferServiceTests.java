package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.TransferRequest;
import com.example.banksystemapi.dto.TransferResponse;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.Transfer;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.Currency;
import com.example.banksystemapi.enums.TransferStatus;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.exception.TransferNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferServiceTests {

    private final TransferRepository transferRepository = mock(TransferRepository.class);
    private final BankAccountRepository bankAccountRepository = mock(BankAccountRepository.class);
    private final TransferService transferService =
            new TransferService(transferRepository, bankAccountRepository);

    @Test
    void transferUpdatesBothBalancesAndReturnsDto() {
        BankAccount source = account(1L, "100.00", AccountStatus.ACTIVE, Currency.AZN);
        BankAccount destination = account(2L, "25.00", AccountStatus.ACTIVE, Currency.AZN);
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(bankAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setId(9L);
            return transfer;
        });

        TransferResponse response = transferService.createTransfer(request(1L, 2L, "30.00"));

        assertEquals(new BigDecimal("70.00"), source.getBalance());
        assertEquals(new BigDecimal("55.00"), destination.getBalance());
        assertEquals(9L, response.getId());
        assertEquals(1L, response.getSourceAccountId());
        assertEquals(2L, response.getDestinationAccountId());
        assertEquals(TransferStatus.COMPLETED, response.getStatus());
        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        assertEquals(new BigDecimal("30.00"), transferCaptor.getValue().getAmount());
    }

    @Test
    void transferRejectsSameAccount() {
        assertThrows(IllegalArgumentException.class,
                () -> transferService.createTransfer(request(1L, 1L, "1.00")));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void transferRejectsInsufficientFundsWithoutChangingBalances() {
        BankAccount source = account(1L, "10.00", AccountStatus.ACTIVE, Currency.AZN);
        BankAccount destination = account(2L, "25.00", AccountStatus.ACTIVE, Currency.AZN);
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(bankAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.createTransfer(request(1L, 2L, "11.00")));

        assertEquals(new BigDecimal("10.00"), source.getBalance());
        assertEquals(new BigDecimal("25.00"), destination.getBalance());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void transferRejectsInactiveAccountWithoutChangingBalances() {
        BankAccount source = account(1L, "10.00", AccountStatus.BLOCKED, Currency.AZN);
        BankAccount destination = account(2L, "25.00", AccountStatus.ACTIVE, Currency.AZN);
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(bankAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.createTransfer(request(1L, 2L, "1.00")));

        assertEquals(new BigDecimal("10.00"), source.getBalance());
        assertEquals(new BigDecimal("25.00"), destination.getBalance());
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void transferRejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> transferService.createTransfer(request(1L, 2L, "0.00")));
    }

    @Test
    void transferRejectsDifferentCurrencies() {
        BankAccount source = account(1L, "100.00", AccountStatus.ACTIVE, Currency.AZN);
        BankAccount destination = account(2L, "25.00", AccountStatus.ACTIVE, Currency.USD);
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(bankAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.createTransfer(request(1L, 2L, "1.00")));

        assertEquals(new BigDecimal("100.00"), source.getBalance());
        assertEquals(new BigDecimal("25.00"), destination.getBalance());
    }

    @Test
    void transferRejectsMissingAccount() {
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> transferService.createTransfer(request(1L, 2L, "1.00")));
    }

    @Test
    void getTransferByIdRejectsMissingTransfer() {
        when(transferRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransferNotFoundException.class, () -> transferService.getTransferById(99L));
    }

    @Test
    void getAccountTransfersReturnsHistoryForEitherSide() {
        when(bankAccountRepository.existsById(1L)).thenReturn(true);
        when(transferRepository
                .findBySourceAccount_IdOrDestinationAccount_IdOrderByCreatedAtDesc(1L, 1L))
                .thenReturn(List.of(transfer(5L, account(1L, "1.00", AccountStatus.ACTIVE, Currency.AZN),
                        account(2L, "1.00", AccountStatus.ACTIVE, Currency.AZN))));

        List<TransferResponse> history = transferService.getAccountTransfers(1L);

        assertEquals(1, history.size());
        assertEquals(1L, history.getFirst().getSourceAccountId());
        assertEquals(2L, history.getFirst().getDestinationAccountId());
    }

    private BankAccount account(Long id, String balance, AccountStatus status, Currency currency) {
        BankAccount account = new BankAccount();
        account.setId(id);
        account.setBalance(new BigDecimal(balance));
        account.setStatus(status);
        account.setCurrency(currency);
        return account;
    }

    private Transfer transfer(Long id, BankAccount source, BankAccount destination) {
        Transfer transfer = new Transfer();
        transfer.setId(id);
        transfer.setSourceAccount(source);
        transfer.setDestinationAccount(destination);
        transfer.setAmount(new BigDecimal("10.00"));
        transfer.setStatus(TransferStatus.COMPLETED);
        return transfer;
    }

    private TransferRequest request(Long sourceId, Long destinationId, String amount) {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(sourceId);
        request.setDestinationAccountId(destinationId);
        request.setAmount(new BigDecimal(amount));
        return request;
    }
}
