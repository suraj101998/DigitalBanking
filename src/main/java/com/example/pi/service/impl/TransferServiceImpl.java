package com.example.pi.service.impl;

import com.example.pi.dto.request.TransferRequest;
import com.example.pi.dto.response.TransferResponse;
import com.example.pi.entity.BankAccount;
import com.example.pi.entity.IdempotencyKey;
import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.AccountStatus;
import com.example.pi.entity.enums.TransactionMode;
import com.example.pi.entity.enums.TransactionStatus;
import com.example.pi.entity.enums.TransactionType;
import com.example.pi.exception.AccountNotFoundException;
import com.example.pi.exception.ConcurrentTransactionException;
import com.example.pi.exception.DuplicateTransactionException;
import com.example.pi.exception.InsufficientBalanceException;
import com.example.pi.exception.InvalidTransactionException;
import com.example.pi.repository.BankAccountRepository;
import com.example.pi.repository.IdempotencyKeyRepository;
import com.example.pi.repository.TransactionsRepository;
import com.example.pi.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Atomic fund transfer service (doc items #8, #9).
 *
 * Correctness guarantees:
 *  1. PESSIMISTIC_WRITE lock on both accounts before any read-modify-write.
 *  2. Locks acquired in ascending account-id order to prevent A→B / B→A deadlocks.
 *  3. Both debit + credit happen inside a single @Transactional boundary.
 *  4. Idempotency key prevents double-processing on client retries.
 *  5. @Version on BankAccount provides optimistic locking as a last-resort safety net.
 */
@Service
public class TransferServiceImpl implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final BankAccountRepository    bankAccountRepository;
    private final TransactionsRepository   transactionsRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper             objectMapper;

    public TransferServiceImpl(BankAccountRepository bankAccountRepository,
                                TransactionsRepository transactionsRepository,
                                IdempotencyKeyRepository idempotencyKeyRepository,
                                ObjectMapper objectMapper) {
        this.bankAccountRepository    = bankAccountRepository;
        this.transactionsRepository   = transactionsRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.objectMapper             = objectMapper;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request, String headerIdempotencyKey) {

        // ── Resolve idempotency key ────────────────────────────────────────────
        String iKey = resolveIdempotencyKey(headerIdempotencyKey, request.getIdempotencyKey());

        if (iKey != null) {
            var existing = idempotencyKeyRepository.findByIdempotencyKey(iKey);
            if (existing.isPresent()) {
                logger.info("Idempotency replay for key={}", iKey);
                return deserializeResponse(existing.get().getResponseBody());
            }
        }

        // ── Validate input ────────────────────────────────────────────────────
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new InvalidTransactionException("Source and destination accounts must be different");
        }

        TransactionMode mode;
        try {
            mode = TransactionMode.valueOf(request.getTransactionMode().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransactionException(
                    "Invalid transaction mode '" + request.getTransactionMode() + "'");
        }

        // ── Load accounts ─────────────────────────────────────────────────────
        BankAccount source = bankAccountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Source account not found: " + request.getSourceAccountNumber()));
        BankAccount dest = bankAccountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Destination account not found: " + request.getDestinationAccountNumber()));

        // ── Acquire PESSIMISTIC_WRITE locks in deterministic order (doc item #8) ──
        // Sorting by id prevents A→B and B→A concurrent transfers from deadlocking.
        BankAccount first  = source.getId() < dest.getId() ? source : dest;
        BankAccount second = source.getId() < dest.getId() ? dest   : source;

        BankAccount lockedFirst = bankAccountRepository.findByCustomerIdForUpdate(first.getCustomerId())
                .orElseThrow(() -> new AccountNotFoundException(first.getCustomerId()));
        BankAccount lockedSecond = bankAccountRepository.findByCustomerIdForUpdate(second.getCustomerId())
                .orElseThrow(() -> new AccountNotFoundException(second.getCustomerId()));

        // Re-map locked instances back to source/dest
        BankAccount lockedSource = lockedFirst.getId().equals(source.getId()) ? lockedFirst : lockedSecond;
        BankAccount lockedDest   = lockedFirst.getId().equals(dest.getId())   ? lockedFirst : lockedSecond;

        // ── Validate account statuses ─────────────────────────────────────────
        if (lockedSource.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException(
                    "Source account is " + lockedSource.getStatus());
        }
        if (lockedDest.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException(
                    "Destination account is " + lockedDest.getStatus());
        }

        // ── Balance check ─────────────────────────────────────────────────────
        BigDecimal amount = request.getAmount();
        if (lockedSource.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + lockedSource.getBalance()
                    + ", Requested: " + amount);
        }

        // ── Apply debit + credit atomically ───────────────────────────────────
        BigDecimal sourceBalanceBefore = lockedSource.getBalance();
        BigDecimal destBalanceBefore   = lockedDest.getBalance();

        lockedSource.setBalance(sourceBalanceBefore.subtract(amount));
        lockedDest.setBalance(destBalanceBefore.add(amount));

        try {
            bankAccountRepository.save(lockedSource);
            bankAccountRepository.save(lockedDest);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConcurrentTransactionException(
                    "Concurrent modification detected. Please retry.");
        }

        Instant now = Instant.now();
        String transferId = "TRF" + UUID.randomUUID().toString().replace("-", "").toUpperCase();

        // ── Debit ledger entry ────────────────────────────────────────────────
        Transactions debitEntry = new Transactions();
        debitEntry.setTransactionId("TXN" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        debitEntry.setCustomerId(lockedSource.getCustomerId());
        debitEntry.setTransactionType(TransactionType.DEBIT);
        debitEntry.setTransactionMode(mode);
        debitEntry.setTransactionTo(request.getDestinationAccountNumber());
        debitEntry.setTransactionAmount(amount);
        debitEntry.setInitialDeposit(sourceBalanceBefore);
        debitEntry.setAvailableBalance(lockedSource.getBalance());
        debitEntry.setTransactionDate(now);
        debitEntry.setTransactionStatus(TransactionStatus.SUCCESS);
        if (iKey != null) debitEntry.setIdempotencyKey(iKey + "-debit");

        // ── Credit ledger entry ───────────────────────────────────────────────
        Transactions creditEntry = new Transactions();
        creditEntry.setTransactionId("TXN" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        creditEntry.setCustomerId(lockedDest.getCustomerId());
        creditEntry.setTransactionType(TransactionType.CREDIT);
        creditEntry.setTransactionMode(mode);
        creditEntry.setTransactionTo(request.getSourceAccountNumber());
        creditEntry.setTransactionAmount(amount);
        creditEntry.setInitialDeposit(destBalanceBefore);
        creditEntry.setAvailableBalance(lockedDest.getBalance());
        creditEntry.setTransactionDate(now);
        creditEntry.setTransactionStatus(TransactionStatus.SUCCESS);
        if (iKey != null) creditEntry.setIdempotencyKey(iKey + "-credit");

        Transactions savedDebit  = transactionsRepository.save(debitEntry);
        Transactions savedCredit = transactionsRepository.save(creditEntry);

        // ── Build response ────────────────────────────────────────────────────
        TransferResponse response = new TransferResponse(
                transferId,
                savedDebit.getTransactionId(),
                savedCredit.getTransactionId(),
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                amount,
                mode.name(),
                TransactionStatus.SUCCESS.name(),
                now,
                iKey
        );

        // ── Store idempotency record ──────────────────────────────────────────
        if (iKey != null) {
            try {
                String json = objectMapper.writeValueAsString(response);
                idempotencyKeyRepository.save(
                        new IdempotencyKey(iKey, transferId, 201, json, lockedSource.getCustomerId()));
            } catch (Exception e) {
                logger.warn("Failed to serialise idempotency response for key={}: {}", iKey, e.getMessage());
            }
        }

        logger.info("Transfer {} completed: {} → {}, amount={}, src_balance={}, dst_balance={}",
                transferId,
                request.getSourceAccountNumber(), request.getDestinationAccountNumber(),
                amount, lockedSource.getBalance(), lockedDest.getBalance());

        return response;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String resolveIdempotencyKey(String headerKey, String bodyKey) {
        if (headerKey != null && !headerKey.isBlank()) return headerKey.trim();
        if (bodyKey   != null && !bodyKey.isBlank())   return bodyKey.trim();
        return null;
    }

    private TransferResponse deserializeResponse(String json) {
        try {
            return objectMapper.readValue(json, TransferResponse.class);
        } catch (Exception e) {
            throw new DuplicateTransactionException(
                    "Idempotency key already used but response cannot be replayed");
        }
    }
}
