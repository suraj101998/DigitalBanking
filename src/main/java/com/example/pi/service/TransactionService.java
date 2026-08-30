package com.example.pi.service;

import org.springframework.data.domain.Pageable;

import com.example.pi.entity.Transactions;
import com.example.pi.dto.response.BalanceResponse;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.TransactionResponse;

import java.math.BigDecimal;

public interface TransactionService {

    /**
     * Create a transaction with atomic balance update via pessimistic locking.
     * transactionAmount is BigDecimal (doc item #4).
     */
    TransactionResponse createTransaction(int customerId, String transactionType, String transactionTo,
                                          BigDecimal transactionAmount, String transactionMode);

    /**
     * Idempotency-aware variant — supply the client Idempotency-Key value.
     * If the same key was used before, the original response is replayed (doc item #7).
     */
    TransactionResponse createTransaction(int customerId, String transactionType, String transactionTo,
                                          BigDecimal transactionAmount, String transactionMode,
                                          String idempotencyKey);

    /**
     * Get paginated list of transactions for a customer.
     */
    PaginationResponse<Transactions> getCustomerTransactions(int customerId, Pageable pageable);

    /**
     * Get current balance for a customer — reads from BankAccount, not last transaction.
     */
    BalanceResponse getBalance(int customerId);

    /**
     * Get transactions filtered by mode with pagination.
     */
    PaginationResponse<Transactions> getTransactionsByType(String transactionMode, Pageable pageable);
}
