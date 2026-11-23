package com.example.banksystemapi.repository;

import com.example.banksystemapi.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySourceAccount_IdOrDestinationAccount_IdOrderByCreatedAtDesc(
            Long sourceAccountId, Long destinationAccountId);
}
