package com.example.pi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.TransactionMode;

/**
 * Repository for Transactions.
 *
 * Uses JPQL instead of native SELECT * (doc items #16, #17).
 * The composite index on (customer_id, transaction_date DESC) is defined on the entity (doc item #18).
 */
@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {

    /** Find a single transaction by its business transaction ID. */
    java.util.Optional<Transactions> findByTransactionId(String transactionId);

    /** Returns all transactions for a customer, newest first. */
    @Query("SELECT t FROM Transactions t WHERE t.customerId = :customerId ORDER BY t.transactionDate DESC")
    Page<Transactions> findByCustomerId(@Param("customerId") int customerId, Pageable pageable);

    /** Used by balance check — returns the single latest transaction for a customer. */
    @Query("SELECT t FROM Transactions t WHERE t.customerId = :customerId ORDER BY t.transactionDate DESC")
    List<Transactions> findLatestByCustomerId(@Param("customerId") int customerId, Pageable pageable);

    /** Returns transactions filtered by transaction mode (enum). */
    @Query("SELECT t FROM Transactions t WHERE t.transactionMode = :mode")
    Page<Transactions> findByTransactionMode(@Param("mode") TransactionMode mode, Pageable pageable);

    // ── Legacy method names kept so existing service calls compile ─────────────

    /** @deprecated Use {@link #findByCustomerId} instead. */
    @Deprecated
    @Query("SELECT t FROM Transactions t WHERE t.customerId = :customerId ORDER BY t.transactionDate DESC")
    Page<Transactions> getCustomerByID(@Param("customer_id") int customerId, Pageable pageable);

    /** @deprecated Use {@link #findLatestByCustomerId} instead. */
    @Deprecated
    @Query("SELECT t FROM Transactions t WHERE t.customerId = :customerId ORDER BY t.transactionDate DESC")
    List<Transactions> getCheckBalance(@Param("customer_id") int customerId);

    /** @deprecated Use {@link #findByTransactionMode} instead. */
    @Deprecated
    @Query("SELECT t FROM Transactions t WHERE t.transactionMode = :mode")
    Page<Transactions> getTransactionByType(@Param("transaction_mode") TransactionMode mode, Pageable pageable);
}
