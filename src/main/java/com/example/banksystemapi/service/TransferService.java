package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.TransferRequest;
import com.example.banksystemapi.dto.TransferResponse;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.Transfer;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.TransferStatus;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.exception.TransferNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional
    public TransferResponse createTransfer(TransferRequest request) {
        Long sourceId = request.getSourceAccountId();
        Long destinationId = request.getDestinationAccountId();
        BigDecimal amount = request.getAmount();

        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }
        if (amount == null || amount.signum() <= 0
                || amount.stripTrailingZeros().scale() > 2
                || amount.stripTrailingZeros().precision() - amount.stripTrailingZeros().scale() > 17) {
            throw new IllegalArgumentException(
                    "Transfer amount must be positive and have at most 17 integer and 2 decimal digits");
        }

        BankAccount firstLockedAccount = getAccountForUpdate(
                sourceId.compareTo(destinationId) < 0 ? sourceId : destinationId);
        BankAccount secondLockedAccount = getAccountForUpdate(
                sourceId.compareTo(destinationId) < 0 ? destinationId : sourceId);
        BankAccount source = sourceId.equals(firstLockedAccount.getId())
                ? firstLockedAccount : secondLockedAccount;
        BankAccount destination = destinationId.equals(firstLockedAccount.getId())
                ? firstLockedAccount : secondLockedAccount;

        ensureActive(source);
        ensureActive(destination);
        if (source.getCurrency() != destination.getCurrency()) {
            throw new IllegalArgumentException("Transfers require matching account currencies");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        bankAccountRepository.save(source);
        bankAccountRepository.save(destination);

        Transfer transfer = new Transfer();
        transfer.setSourceAccount(source);
        transfer.setDestinationAccount(destination);
        transfer.setAmount(amount);
        transfer.setStatus(TransferStatus.COMPLETED);
        return mapToResponse(transferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + id));
        return mapToResponse(transfer);
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getAccountTransfers(Long accountId) {
        if (!bankAccountRepository.existsById(accountId)) {
            throw new BankAccountNotFoundException("Bank account not found with id: " + accountId);
        }
        return transferRepository
                .findBySourceAccount_IdOrDestinationAccount_IdOrderByCreatedAtDesc(accountId, accountId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BankAccount getAccountForUpdate(Long id) {
        return bankAccountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with id: " + id));
    }

    private void ensureActive(BankAccount account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Bank account is not active: " + account.getId());
        }
    }

    private TransferResponse mapToResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setSourceAccountId(transfer.getSourceAccount().getId());
        response.setDestinationAccountId(transfer.getDestinationAccount().getId());
        response.setAmount(transfer.getAmount());
        response.setStatus(transfer.getStatus());
        response.setCreatedAt(transfer.getCreatedAt());
        return response;
    }
}
