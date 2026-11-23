
package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.TransactionRequest;
import com.example.banksystemapi.dto.TransactionResponse;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.Transaction;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.TransactionStatus;
import com.example.banksystemapi.enums.TransactionType;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {
        validateAmount(request.getAmount());

        BankAccount account = getActiveAccount(request.getAccountId());

        account.setBalance(account.getBalance().add(request.getAmount()));
        bankAccountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setBankAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse withdraw(TransactionRequest request) {
        validateAmount(request.getAmount());

        BankAccount account = getActiveAccount(request.getAccountId());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        bankAccountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setBankAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setStatus(TransactionStatus.COMPLETED);

        return mapToResponse(transactionRepository.save(transaction));
    }

    public List<TransactionResponse> getAccountTransactions(Long accountId) {

        if (!bankAccountRepository.existsById(accountId)) {
            throw new BankAccountNotFoundException("Bank account not found with id: " + accountId);
        }

        return transactionRepository
                .findByBankAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BankAccount getActiveAccount(Long accountId) {

        BankAccount account = bankAccountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException(
                        "Bank account not found with id: " + accountId
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Bank account is not active");
        }

        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0
                || amount.stripTrailingZeros().scale() > 2
                || amount.stripTrailingZeros().precision() - amount.stripTrailingZeros().scale() > 17) {
            throw new IllegalArgumentException(
                    "Transaction amount must be positive and have at most 17 integer and 2 decimal digits");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {

        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setAccountId(transaction.getBankAccount().getId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }
}
