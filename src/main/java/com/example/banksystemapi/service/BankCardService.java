package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.BankCardRequest;
import com.example.banksystemapi.dto.BankCardResponse;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.BankCard;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.CardStatus;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.exception.BankCardNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.BankCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankCardService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final BankCardRepository bankCardRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional
    public BankCardResponse createCard(BankCardRequest request) {
        BankAccount account = bankAccountRepository.findByIdForUpdate(request.getBankAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException(
                        "Bank account not found with id: " + request.getBankAccountId()
                ));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("A card can only be created for an active account");
        }

        BankCard card = new BankCard();
        card.setCardNumber(generateCardNumber());
        card.setStatus(CardStatus.ACTIVE);
        card.setBankAccount(account);
        return mapToResponse(bankCardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<BankCardResponse> getAllCards() {
        return bankCardRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BankCardResponse getCardById(Long id) {
        BankCard card = bankCardRepository.findById(id)
                .orElseThrow(() -> new BankCardNotFoundException("Card not found with id: " + id));
        return mapToResponse(card);
    }

    @Transactional(readOnly = true)
    public List<BankCardResponse> getCardsByAccountId(Long accountId) {
        if (!bankAccountRepository.existsById(accountId)) {
            throw new BankAccountNotFoundException("Bank account not found with id: " + accountId);
        }
        return bankCardRepository.findAllByBankAccount_Id(accountId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BankCardResponse updateCardStatus(Long id, CardStatus status) {
        BankCard card = bankCardRepository.findById(id)
                .orElseThrow(() -> new BankCardNotFoundException("Card not found with id: " + id));
        card.setStatus(status);
        return mapToResponse(bankCardRepository.save(card));
    }

    private String generateCardNumber() {
        String cardNumber;
        do {
            String base = randomBaseNumber();
            cardNumber = base + luhnCheckDigit(base);
        } while (bankCardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    private String randomBaseNumber() {
        StringBuilder number = new StringBuilder(15);
        number.append(RANDOM.nextInt(9) + 1);
        for (int i = 1; i < 15; i++) {
            number.append(RANDOM.nextInt(10));
        }
        return number.toString();
    }

    private int luhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - sum % 10) % 10;
    }

    private BankCardResponse mapToResponse(BankCard card) {
        BankCardResponse response = new BankCardResponse();
        response.setId(card.getId());
        response.setMaskedCardNumber("************" + card.getCardNumber().substring(12));
        response.setStatus(card.getStatus());
        response.setBankAccountId(card.getBankAccount().getId());
        response.setCreatedAt(card.getCreatedAt());
        return response;
    }
}
