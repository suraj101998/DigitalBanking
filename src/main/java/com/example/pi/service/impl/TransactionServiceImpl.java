package com.example.pi.service.impl;

import com.example.pi.config.TransactionLimitConfig;
import com.example.pi.entity.BankAccount;
import com.example.pi.entity.IdempotencyKey;
import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.TransactionStatus;
import com.example.pi.entity.enums.TransactionType;
import com.example.pi.entity.enums.TransactionMode;
import com.example.pi.entity.enums.AccountStatus;
import com.example.pi.exception.AccountNotFoundException;
import com.example.pi.exception.ConcurrentTransactionException;
import com.example.pi.exception.CustomerNotFoundException;
import com.example.pi.exception.DuplicateTransactionException;
import com.example.pi.exception.InsufficientBalanceException;
import com.example.pi.exception.InvalidTransactionException;
import com.example.pi.repository.BankAccountRepository;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.repository.IdempotencyKeyRepository;
import com.example.pi.repository.TransactionsRepository;
import com.example.pi.service.TransactionService;
import com.example.pi.util.PaginationUtil;
import com.example.pi.dto.response.BalanceResponse;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.TransactionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction service implementation.
 *
 * Key improvements:
 *  - Balance lives in BankAccount, never in last-transaction (doc items #2, #3, #20)
 *  - Pessimistic locking via findByCustomerIdForUpdate (doc item #6)
 *  - Optimistic locking (@Version on BankAccount) as safety net (doc item #61)
 *  - Idempotency — duplicate keys replay the original response (doc item #7)
 *  - BigDecimal throughout (doc item #4)
 *  - Instant timestamps (doc item #19)
 *  - TransactionStatus enum (doc item #21)
 *  - Micrometer metrics for every operation outcome (doc item #51)
 *  - Domain exceptions instead of generic Exception (doc item #29)
 *  - Constructor injection (doc item #30)
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionsRepository   transactionsRepository;
    private final CustomerRepository       customerRepository;
    private final BankAccountRepository    bankAccountRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper             objectMapper;
    private final TransactionLimitConfig   limitConfig;

    // ── Micrometer metrics (doc item #51) ─────────────────────────────────────
    private final Counter txSuccessCounter;
    private final Counter txFailureCounter;
    private final Counter txInsufficientBalanceCounter;
    private final Timer   txTimer;

    public TransactionServiceImpl(TransactionsRepository transactionsRepository,
                                   CustomerRepository customerRepository,
                                   BankAccountRepository bankAccountRepository,
                                   IdempotencyKeyRepository idempotencyKeyRepository,
                                   ObjectMapper objectMapper,
                                   MeterRegistry meterRegistry,
                                   TransactionLimitConfig limitConfig) {
        this.transactionsRepository   = transactionsRepository;
        this.customerRepository       = customerRepository;
        this.bankAccountRepository    = bankAccountRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.objectMapper             = objectMapper;
        this.limitConfig              = limitConfig;

        this.txSuccessCounter            = meterRegistry.counter("transaction.success.count");
        this.txFailureCounter            = meterRegistry.counter("transaction.failure.count");
        this.txInsufficientBalanceCounter= meterRegistry.counter("transaction.insufficient_balance.count");
        this.txTimer                     = meterRegistry.timer("transaction.duration");
    }

    /**
     * Creates a credit or debit transaction with atomic balance update.
     *
     * Supports idempotency: if the same Idempotency-Key is seen again the original
     * TransactionResponse is returned without processing a second transaction (doc item #7).
     */
    @Override
    @Transactional
    public TransactionResponse createTransaction(int customerId, String transactionType,
                                                 String transactionTo, BigDecimal transactionAmount,
                                                 String transactionMode) {
        return createTransaction(customerId, transactionType, transactionTo,
                                 transactionAmount, transactionMode, null);
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(int customerId, String transactionType,
                                                 String transactionTo, BigDecimal transactionAmount,
                                                 String transactionMode, String idempotencyKeyValue) {
        return txTimer.record(() -> doCreateTransaction(
                customerId, transactionType, transactionTo,
                transactionAmount, transactionMode, idempotencyKeyValue));
    }

    private TransactionResponse doCreateTransaction(int customerId, String transactionType,
                                                     String transactionTo, BigDecimal transactionAmount,
                                                     String transactionMode, String iKey) {
        // ── Idempotency check ──────────────────────────────────────────────────
        if (iKey != null && !iKey.isBlank()) {
            var existing = idempotencyKeyRepository.findByIdempotencyKey(iKey);
            if (existing.isPresent()) {
                logger.info("Idempotency replay for key={}", iKey);
                return deserializeResponse(existing.get().getResponseBody());
            }
        }

        // ── Validate customer ──────────────────────────────────────────────────
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // ── Validate amount + limits (doc item #63) ────────────────────────────
        if (transactionAmount == null || transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be positive");
        }
        if (transactionAmount.compareTo(limitConfig.getMaxSingleAmount()) > 0) {
            throw new InvalidTransactionException(
                    "Transaction amount " + transactionAmount
                    + " exceeds single-transaction limit of " + limitConfig.getMaxSingleAmount());
        }

        // ── Validate enums ─────────────────────────────────────────────────────
        TransactionType type;
        try {
            type = TransactionType.valueOf(transactionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransactionException(
                    "Invalid transaction type '" + transactionType + "'. Must be CREDIT or DEBIT");
        }

        TransactionMode mode;
        try {
            mode = TransactionMode.valueOf(transactionMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransactionException(
                    "Invalid transaction mode '" + transactionMode
                    + "'. Valid values: UPI, NEFT, RTGS, IMPS, CASH, CHEQUE, ONLINE");
        }

        // ── Pessimistic lock on account ────────────────────────────────────────
        BankAccount account = bankAccountRepository.findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new AccountNotFoundException(customerId));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException(
                    "Account is " + account.getStatus() + " and cannot process transactions");
        }

        BigDecimal currentBalance = account.getBalance();

        // ── Balance calculation ────────────────────────────────────────────────
        BigDecimal newBalance;
        if (type == TransactionType.DEBIT) {
            if (currentBalance.compareTo(transactionAmount) < 0) {
                txInsufficientBalanceCounter.increment();
                txFailureCounter.increment();
                throw new InsufficientBalanceException(
                        "Insufficient balance. Current: " + currentBalance
                        + ", Requested: " + transactionAmount);
            }
            newBalance = currentBalance.subtract(transactionAmount);
        } else {
            newBalance = currentBalance.add(transactionAmount);
        }

        // ── Update account balance ─────────────────────────────────────────────
        account.setBalance(newBalance);
        try {
            bankAccountRepository.save(account);
        } catch (ObjectOptimisticLockingFailureException e) {
            txFailureCounter.increment();
            throw new ConcurrentTransactionException(
                    "Concurrent modification detected. Please retry the transaction.");
        }

        // ── Save immutable transaction record ──────────────────────────────────
        Transactions transaction = new Transactions();
        transaction.setTransactionId(generateTransactionId());
        transaction.setCustomerId(customerId);
        transaction.setTransactionType(type);
        transaction.setTransactionTo(transactionTo);
        transaction.setTransactionAmount(transactionAmount);
        transaction.setTransactionMode(mode);
        transaction.setTransactionDate(Instant.now());
        transaction.setInitialDeposit(currentBalance);
        transaction.setAvailableBalance(newBalance);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        if (iKey != null && !iKey.isBlank()) {
            transaction.setIdempotencyKey(iKey);
        }

        Transactions saved = transactionsRepository.save(transaction);

        logger.info("Transaction {} created: type={}, amount={}, customerId={}, newBalance={}",
                saved.getTransactionId(), type, transactionAmount, customerId, newBalance);

        txSuccessCounter.increment();

        TransactionResponse response = mapToTransactionResponse(saved);

        // ── Store idempotency record ───────────────────────────────────────────
        if (iKey != null && !iKey.isBlank()) {
            try {
                String json = objectMapper.writeValueAsString(response);
                idempotencyKeyRepository.save(
                        new IdempotencyKey(iKey, saved.getTransactionId(), 201, json, customerId));
            } catch (Exception e) {
                logger.warn("Failed to store idempotency record for key={}: {}", iKey, e.getMessage());
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<Transactions> getCustomerTransactions(int customerId, Pageable pageable) {
        Page<Transactions> page = transactionsRepository.findByCustomerId(customerId, pageable);
        return PaginationUtil.buildPaginationResponse(page);
    }

    /** Balance read from BankAccount — never from last transaction (doc item #20). */
    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(int customerId) {
        BankAccount account = bankAccountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AccountNotFoundException(customerId));

        return new BalanceResponse(
                customerId,
                account.getBalance(),
                account.getInitialDeposit(),
                account.getCurrency(),
                account.getStatus().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<Transactions> getTransactionsByType(String transactionMode, Pageable pageable) {
        TransactionMode mode;
        try {
            mode = TransactionMode.valueOf(transactionMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransactionException(
                    "Invalid transaction mode '" + transactionMode
                    + "'. Valid values: UPI, NEFT, RTGS, IMPS, CASH, CHEQUE, ONLINE");
        }
        Page<Transactions> page = transactionsRepository.findByTransactionMode(mode, pageable);
        return PaginationUtil.buildPaginationResponse(page);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private TransactionResponse mapToTransactionResponse(Transactions t) {
        return new TransactionResponse(
                t.getSerialNumber(),
                t.getTransaction_type(),
                t.getTransactionId(),
                t.getTransactionTo(),
                t.getTransactionDate(),
                t.getTransactionAmount(),
                t.getCustomerId(),
                t.getInitialDeposit(),
                t.getAvailableBalance(),
                t.getTransaction_mode(),
                t.getTransactionStatus() != null ? t.getTransactionStatus().name() : null
        );
    }

    private TransactionResponse deserializeResponse(String json) {
        try {
            return objectMapper.readValue(json, TransactionResponse.class);
        } catch (Exception e) {
            throw new DuplicateTransactionException(
                    "Idempotency key already used but response cannot be replayed");
        }
    }
}
