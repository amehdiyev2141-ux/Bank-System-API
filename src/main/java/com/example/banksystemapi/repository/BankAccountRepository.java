
package com.example.banksystemapi.repository;

import com.example.banksystemapi.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    boolean existsByAccountNumber(String accountNumber);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findAllByCustomer_Id(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from BankAccount account where account.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);
}
