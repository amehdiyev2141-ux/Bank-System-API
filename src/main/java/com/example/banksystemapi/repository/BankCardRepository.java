package com.example.banksystemapi.repository;

import com.example.banksystemapi.entity.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    boolean existsByCardNumber(String cardNumber);

    List<BankCard> findAllByBankAccount_Id(Long bankAccountId);
}
