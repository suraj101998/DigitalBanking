package com.example.pi.service;

import org.springframework.data.domain.Pageable;

import com.example.pi.entity.Transactions;
import com.example.pi.dto.response.BalanceResponse;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.TransactionResponse;

public interface TransactionService {

    /**
     * Create a transaction with automatic balance calculation
     */
    TransactionResponse createTransaction(int customerId, String transactionType, String transactionTo,
                                         long transactionAmount, String transactionMode) throws Exception;

    /**
     * Get paginated list of transactions for a customer
     */
    PaginationResponse<Transactions> getCustomerTransactions(int customerId, Pageable pageable);

    /**
     * Get current balance for a customer
     */
    BalanceResponse getBalance(int customerId);

    /**
     * Get transactions filtered by type with pagination
     */
    PaginationResponse<Transactions> getTransactionsByType(String transactionMode, Pageable pageable);
}
