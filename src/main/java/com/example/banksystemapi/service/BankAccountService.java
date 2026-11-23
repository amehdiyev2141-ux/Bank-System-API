
package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.BankAccountRequest;
import com.example.banksystemapi.dto.BankAccountResponse;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.Customer;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.exception.CustomerNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public BankAccountResponse createAccount(BankAccountRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()
                ));

        BankAccount account = new BankAccount();

        account.setAccountNumber(generateAccountNumber());
        account.setBalance(new BigDecimal("0.00"));
        account.setCurrency(request.getCurrency());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        BankAccount savedAccount = bankAccountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAllAccounts() {
        return bankAccountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BankAccountResponse getAccountById(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(
                        "Bank account not found with id: " + id
                ));

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAccountsByCustomerId(Long customerId) {
        return bankAccountRepository.findAllByCustomer_Id(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String generateAccountNumber() {
        String accountNumber;

        do {
            accountNumber = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 16)
                    .toUpperCase();
        } while (bankAccountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private BankAccountResponse mapToResponse(BankAccount account) {

        BankAccountResponse response = new BankAccountResponse();

        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setStatus(account.getStatus());
        response.setCustomerId(account.getCustomer().getId());
        response.setCreatedAt(account.getCreatedAt());

        return response;
    }
}
