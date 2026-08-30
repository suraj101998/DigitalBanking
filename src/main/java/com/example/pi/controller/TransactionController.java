package com.example.pi.controller;

import com.example.pi.dto.response.TransactionResponse;
import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.TransactionStatus;
import com.example.pi.exception.InvalidTransactionException;
import com.example.pi.exception.ResourceNotFoundException;
import com.example.pi.repository.TransactionsRepository;
import com.example.pi.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction controller (doc items #5, #13, #21, #78).
 *
 * Separate from CustomerController as recommended (doc item #42).
 * Handles transaction-level operations:
 *  - GET single transaction
 *  - POST reverse a transaction (creates a reversal credit/debit entry — doc item #78)
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionsRepository transactionsRepository;
    private final TransactionService     transactionService;

    public TransactionController(TransactionsRepository transactionsRepository,
                                  TransactionService transactionService) {
        this.transactionsRepository = transactionsRepository;
        this.transactionService     = transactionService;
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Transactions> getTransaction(@PathVariable String transactionId) {
        return transactionsRepository.findByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found: " + transactionId));
    }

    /**
     * Reverse a committed transaction (doc item #78).
     *
     * Creates an equal and opposite ledger entry and marks the original as REVERSED.
     * The reversal itself is a new immutable transaction record (CREDIT for a DEBIT original,
     * DEBIT for a CREDIT original).
     *
     * Only ADMIN can reverse transactions.
     */
    @PostMapping("/{transactionId}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable String transactionId) {

        Transactions original = transactionsRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found: " + transactionId));

        if (original.getTransactionStatus() == TransactionStatus.REVERSED) {
            throw new InvalidTransactionException(
                    "Transaction " + transactionId + " has already been reversed");
        }
        if (original.getTransactionStatus() != TransactionStatus.SUCCESS) {
            throw new InvalidTransactionException(
                    "Only SUCCESS transactions can be reversed. Status: "
                    + original.getTransactionStatus());
        }

        // The reversal is the opposite type: DEBIT → CREDIT, CREDIT → DEBIT
        String reversalType = original.getTransaction_type().equalsIgnoreCase("DEBIT")
                ? "CREDIT" : "DEBIT";

        TransactionResponse reversal = transactionService.createTransaction(
                original.getCustomerId(),
                reversalType,
                "REVERSAL_OF_" + transactionId,
                original.getTransactionAmount(),
                original.getTransaction_mode() != null ? original.getTransaction_mode() : "CASH"
        );

        // Mark the original transaction as REVERSED (the only allowed post-save mutation)
        original.setTransactionStatus(TransactionStatus.REVERSED);
        transactionsRepository.save(original);

        return ResponseEntity.status(HttpStatus.CREATED).body(reversal);
    }
}
