package com.example.banksystemapi.service;

import com.example.banksystemapi.dto.BankCardRequest;
import com.example.banksystemapi.dto.BankCardResponse;
import com.example.banksystemapi.entity.BankAccount;
import com.example.banksystemapi.entity.BankCard;
import com.example.banksystemapi.enums.AccountStatus;
import com.example.banksystemapi.enums.CardStatus;
import com.example.banksystemapi.enums.Currency;
import com.example.banksystemapi.exception.BankAccountNotFoundException;
import com.example.banksystemapi.exception.BankCardNotFoundException;
import com.example.banksystemapi.repository.BankAccountRepository;
import com.example.banksystemapi.repository.BankCardRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

class BankCardServiceTests {

    private final BankCardRepository bankCardRepository = mock(BankCardRepository.class);
    private final BankAccountRepository bankAccountRepository = mock(BankAccountRepository.class);
    private final BankCardService bankCardService =
            new BankCardService(bankCardRepository, bankAccountRepository);

    @Test
    void createCardGeneratesUniqueNumberAndReturnsOnlyMaskedNumber() {
        BankAccount account = account(4L, AccountStatus.ACTIVE);
        LocalDateTime createdAt = LocalDateTime.parse("2026-01-01T10:00:00");
        when(bankAccountRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(account));
        when(bankCardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(bankCardRepository.save(any(BankCard.class))).thenAnswer(invocation -> {
            BankCard card = invocation.getArgument(0);
            card.setId(8L);
            card.setCreatedAt(createdAt);
            return card;
        });

        BankCardResponse response = bankCardService.createCard(request(4L));

        ArgumentCaptor<BankCard> cardCaptor = ArgumentCaptor.forClass(BankCard.class);
        verify(bankCardRepository).save(cardCaptor.capture());
        BankCard savedCard = cardCaptor.getValue();
        assertTrue(savedCard.getCardNumber().matches("[0-9]{16}"));
        assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
        assertEquals(account, savedCard.getBankAccount());
        assertEquals(8L, response.getId());
        assertEquals("************" + savedCard.getCardNumber().substring(12),
                response.getMaskedCardNumber());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals(4L, response.getBankAccountId());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void createCardRetriesIfGeneratedNumberExists() {
        when(bankAccountRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(account(4L, AccountStatus.ACTIVE)));
        when(bankCardRepository.existsByCardNumber(anyString())).thenReturn(true, false);
        when(bankCardRepository.save(any(BankCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bankCardService.createCard(request(4L));

        ArgumentCaptor<String> numberCaptor = ArgumentCaptor.forClass(String.class);
        verify(bankCardRepository, times(2)).existsByCardNumber(numberCaptor.capture());
        assertNotEquals(numberCaptor.getAllValues().get(0), numberCaptor.getAllValues().get(1));
    }

    @Test
    void createCardRequiresExistingActiveAccount() {
        when(bankAccountRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(account(4L, AccountStatus.BLOCKED)));

        assertThrows(IllegalArgumentException.class, () -> bankCardService.createCard(request(4L)));
        verify(bankCardRepository, never()).save(any(BankCard.class));
    }

    @Test
    void createCardRejectsMissingAccount() {
        when(bankAccountRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankCardService.createCard(request(99L)));
    }

    @Test
    void getAllCardsMapsToResponses() {
        when(bankCardRepository.findAll()).thenReturn(List.of(card(8L, 4L)));

        List<BankCardResponse> responses = bankCardService.getAllCards();

        assertEquals(1, responses.size());
        assertEquals(8L, responses.getFirst().getId());
        assertEquals(4L, responses.getFirst().getBankAccountId());
    }

    @Test
    void getCardByIdRejectsMissingCard() {
        when(bankCardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BankCardNotFoundException.class, () -> bankCardService.getCardById(99L));
    }

    @Test
    void getCardsByAccountIdMapsToResponses() {
        when(bankAccountRepository.existsById(4L)).thenReturn(true);
        when(bankCardRepository.findAllByBankAccount_Id(4L)).thenReturn(List.of(card(8L, 4L)));

        List<BankCardResponse> responses = bankCardService.getCardsByAccountId(4L);

        assertEquals(1, responses.size());
        assertEquals(4L, responses.getFirst().getBankAccountId());
    }

    @Test
    void updateCardStatusChangesStatus() {
        BankCard card = card(8L, 4L);
        when(bankCardRepository.findById(8L)).thenReturn(Optional.of(card));
        when(bankCardRepository.save(card)).thenReturn(card);

        BankCardResponse response = bankCardService.updateCardStatus(8L, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, response.getStatus());
    }

    private BankAccount account(Long id, AccountStatus status) {
        BankAccount account = new BankAccount();
        account.setId(id);
        account.setStatus(status);
        account.setCurrency(Currency.AZN);
        return account;
    }

    private BankCard card(Long id, Long accountId) {
        BankCard card = new BankCard();
        card.setId(id);
        card.setCardNumber("1234567890123452");
        card.setStatus(CardStatus.ACTIVE);
        card.setBankAccount(account(accountId, AccountStatus.ACTIVE));
        card.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return card;
    }

    private BankCardRequest request(Long accountId) {
        BankCardRequest request = new BankCardRequest();
        request.setBankAccountId(accountId);
        return request;
    }
}
