package com.example.pi.repository;

import com.example.pi.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for BankAccount.
 *
 * findByCustomerIdForUpdate uses PESSIMISTIC_WRITE locking so concurrent
 * debit/credit operations block rather than producing a lost-update.
 * For transfers, accounts should be locked in ascending id order to avoid deadlocks.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByCustomerId(Integer customerId);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    Optional<BankAccount> findByAccountNumberAndCustomerId(String accountNumber, Integer customerId);

    /**
     * Acquires a row-level exclusive lock on the account row.
     * Use this in any @Transactional service method that modifies balance.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAccount a WHERE a.customerId = :customerId")
    Optional<BankAccount> findByCustomerIdForUpdate(@Param("customerId") Integer customerId);
}
