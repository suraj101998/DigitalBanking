package com.example.pi.service.impl;

import com.example.pi.config.TransactionLimitConfig;
import com.example.pi.entity.BankAccount;
import com.example.pi.entity.Customers;
import com.example.pi.entity.IdempotencyKey;
import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.AccountStatus;
import com.example.pi.entity.enums.TransactionStatus;
import com.example.pi.entity.enums.TransactionType;
import com.example.pi.exception.AccountNotFoundException;
import com.example.pi.exception.InsufficientBalanceException;
import com.example.pi.exception.InvalidTransactionException;
import com.example.pi.repository.BankAccountRepository;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.repository.IdempotencyKeyRepository;
import com.example.pi.repository.TransactionsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionServiceImpl (doc item #54).
 *
 * Tests cover (doc item #54):
 *  - Successful credit
 *  - Successful debit
 *  - Debit greater than balance → InsufficientBalanceException
 *  - Amount exceeds single-transaction limit → InvalidTransactionException
 *  - Unknown customer → AccountNotFoundException
 *  - Idempotency replay returns original response without saving a second transaction
 *  - Invalid transaction type → InvalidTransactionException
 *  - Account not ACTIVE → InvalidTransactionException
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceImplTest {

    @Mock TransactionsRepository   transactionsRepository;
    @Mock CustomerRepository       customerRepository;
    @Mock BankAccountRepository    bankAccountRepository;
    @Mock IdempotencyKeyRepository idempotencyKeyRepository;

    private TransactionServiceImpl service;

    private static final int CUSTOMER_ID = 1;

    @BeforeEach
    void setUp() {
        TransactionLimitConfig limits = new TransactionLimitConfig();
        limits.setMaxSingleAmount(new BigDecimal("500000.00"));
        limits.setDailyLimit(new BigDecimal("1000000.00"));
        limits.setMonthlyLimit(new BigDecimal("5000000.00"));

        service = new TransactionServiceImpl(
                transactionsRepository,
                customerRepository,
                bankAccountRepository,
                idempotencyKeyRepository,
                new ObjectMapper(),
                new SimpleMeterRegistry(),
                limits
        );
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Customers customer() {
        Customers c = new Customers();
        c.setCustomerId(CUSTOMER_ID);
        return c;
    }

    private BankAccount account(BigDecimal balance) {
        BankAccount a = new BankAccount("ACC001", balance, CUSTOMER_ID);
        a.setId(1L);
        return a;
    }

    private Transactions savedTransaction(TransactionType type, BigDecimal amount,
                                          BigDecimal newBalance) {
        Transactions t = new Transactions();
        t.setSerialNumber(1);
        t.setTransactionId("TXN001");
        t.setCustomerId(CUSTOMER_ID);
        t.setTransactionType(type);
        t.setTransactionAmount(amount);
        t.setAvailableBalance(newBalance);
        t.setInitialDeposit(newBalance.add(type == TransactionType.DEBIT ? amount : amount.negate()));
        t.setTransactionStatus(TransactionStatus.SUCCESS);
        return t;
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    void credit_increasesBalance() {
        BigDecimal initial = new BigDecimal("1000.00");
        BigDecimal amount  = new BigDecimal("500.00");

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer()));
        when(bankAccountRepository.findByCustomerIdForUpdate(CUSTOMER_ID))
                .thenReturn(Optional.of(account(initial)));
        when(bankAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionsRepository.save(any()))
                .thenReturn(savedTransaction(TransactionType.CREDIT, amount, initial.add(amount)));
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        var response = service.createTransaction(CUSTOMER_ID, "CREDIT", "Test", amount, "UPI");

        assertThat(response.getAvailableBalance()).isEqualByComparingTo("1500.00");
        verify(bankAccountRepository).save(argThat(a -> ((BankAccount) a)
                .getBalance().compareTo(new BigDecimal("1500.00")) == 0));
    }

    @Test
    void debit_decreasesBalance() {
        BigDecimal initial = new BigDecimal("2000.00");
        BigDecimal amount  = new BigDecimal("300.00");

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer()));
        when(bankAccountRepository.findByCustomerIdForUpdate(CUSTOMER_ID))
                .thenReturn(Optional.of(account(initial)));
        when(bankAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionsRepository.save(any()))
                .thenReturn(savedTransaction(TransactionType.DEBIT, amount, initial.subtract(amount)));
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        var response = service.createTransaction(CUSTOMER_ID, "DEBIT", "Merchant", amount, "UPI");

        assertThat(response.getAvailableBalance()).isEqualByComparingTo("1700.00");
    }

    @Test
    void debit_insufficientBalance_throwsException() {
        BigDecimal balance = new BigDecimal("100.00");
        BigDecimal amount  = new BigDecimal("500.00");

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer()));
        when(bankAccountRepository.findByCustomerIdForUpdate(CUSTOMER_ID))
                .thenReturn(Optional.of(account(balance)));
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createTransaction(CUSTOMER_ID, "DEBIT", "M", amount, "UPI"))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void amount_exceedsSingleLimit_throwsException() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer()));
        when(bankAccountRepository.findByCustomerIdForUpdate(CUSTOMER_ID))
                .thenReturn(Optional.of(account(new BigDecimal("9999999.00"))));
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createTransaction(CUSTOMER_ID, "DEBIT", "M",
                        new BigDecimal("600000.00"), "UPI"))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("exceeds single-transaction limit");
    }

    @Test
    void unknownCustomer_throwsAccountNotFoundException() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer()));
        when(bankAccountRepository.findByCustomerIdForUpdate(CUSTOMER_ID))
                .thenReturn(Optional.empty());
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createTransaction(CUSTOMER_ID, "CREDIT", "X",
                        new BigDecimal("100.00"), "UPI"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void idempotency_replay_doesNotSaveAgain() throws Exception {
        String iKey   = "test-idempotency-key";
        // Wrap in a minimal TransactionResponse-shaped JSON that ObjectMapper can read
        String responseJson = "{\"serialNumber\":1,\"transactionId\":\"TXN001\"," +
                "\"transactionType\":\"CREDIT\",\"transactionAmount\":100," +
                "\"availableBalance\":1100,\"customerId\":1," +
                "\"transactionStatus\":\"SUCCESS\"}";
        IdempotencyKey existingKey = new IdempotencyKey(iKey, "TXN001", 201, responseJson, CUSTOMER_ID);

        when(idempotencyKeyRepository.findByIdempotencyKey(iKey))
                .thenReturn(Optional.of(existingKey));

        var response = service.createTransaction(CUSTOMER_ID, "CREDIT", "X",
                new BigDecimal("100.00"), "UPI", iKey);

        assertThat(response.getTransactionId()).isEqualTo("TXN001");
        verify(transactionsRepository, never()).save(any());
        verify(bankAccountRepository, never()).findByCustomerIdForUpdate(anyInt());
    }

    @Test
    void invalidTransactionType_throwsException() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer()));
        when(bankAccountRepository.findByCustomerIdForUpdate(CUSTOMER_ID))
                .thenReturn(Optional.of(account(new BigDecimal("1000.00"))));
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createTransaction(CUSTOMER_ID, "INVALID_TYPE", "X",
                        new BigDecimal("100.00"), "UPI"))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Invalid transaction type");
    }

    @Test
    void inactiveAccount_throwsException() {
        BankAccount blocked = account(new BigDecimal("1000.00"));
        blocked.setStatus(AccountStatus.BLOCKED);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer()));
        when(bankAccountRepository.findByCustomerIdForUpdate(CUSTOMER_ID))
                .thenReturn(Optional.of(blocked));
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createTransaction(CUSTOMER_ID, "CREDIT", "X",
                        new BigDecimal("100.00"), "UPI"))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("BLOCKED");
    }
}
