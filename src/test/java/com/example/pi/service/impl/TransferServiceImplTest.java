package com.example.pi.service.impl;

import com.example.pi.dto.request.TransferRequest;
import com.example.pi.entity.BankAccount;
import com.example.pi.entity.IdempotencyKey;
import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.AccountStatus;
import com.example.pi.exception.InsufficientBalanceException;
import com.example.pi.exception.InvalidTransactionException;
import com.example.pi.repository.BankAccountRepository;
import com.example.pi.repository.IdempotencyKeyRepository;
import com.example.pi.repository.TransactionsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransferServiceImpl (doc item #55).
 *
 * Covers (doc item #54):
 *  - Successful transfer: source deducted, destination credited
 *  - Insufficient source balance → InsufficientBalanceException
 *  - Same source and destination → InvalidTransactionException
 *  - Blocked account → InvalidTransactionException
 *  - Idempotency replay returns original response
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransferServiceImplTest {

    @Mock BankAccountRepository    bankAccountRepository;
    @Mock TransactionsRepository   transactionsRepository;
    @Mock IdempotencyKeyRepository idempotencyKeyRepository;

    private TransferServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TransferServiceImpl(
                bankAccountRepository,
                transactionsRepository,
                idempotencyKeyRepository,
                new ObjectMapper()
        );
    }

    private BankAccount account(long id, int customerId, String accNum, BigDecimal balance) {
        BankAccount a = new BankAccount(accNum, balance, customerId);
        a.setId(id);
        return a;
    }

    private TransferRequest request(String src, String dst, BigDecimal amount) {
        TransferRequest r = new TransferRequest();
        r.setSourceAccountNumber(src);
        r.setDestinationAccountNumber(dst);
        r.setAmount(amount);
        r.setTransactionMode("NEFT");
        return r;
    }

    @Test
    void successfulTransfer_debitsSourceCreditsDestination() {
        BankAccount src  = account(1L, 1, "ACC001", new BigDecimal("5000.00"));
        BankAccount dst  = account(2L, 2, "ACC002", new BigDecimal("1000.00"));
        BigDecimal  amount = new BigDecimal("1000.00");

        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(bankAccountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(dst));
        when(bankAccountRepository.findByCustomerIdForUpdate(1)).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByCustomerIdForUpdate(2)).thenReturn(Optional.of(dst));
        when(bankAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionsRepository.save(any())).thenAnswer(i -> {
            Transactions t = i.getArgument(0); t.setSerialNumber(1); return t;
        });

        var response = service.transfer(request("ACC001", "ACC002", amount), null);

        assertThat(response.getSourceAccountNumber()).isEqualTo("ACC001");
        assertThat(response.getDestinationAccountNumber()).isEqualTo("ACC002");
        assertThat(src.getBalance()).isEqualByComparingTo("4000.00");
        assertThat(dst.getBalance()).isEqualByComparingTo("2000.00");
        verify(transactionsRepository, times(2)).save(any());
    }

    @Test
    void insufficientBalance_throwsException() {
        BankAccount src = account(1L, 1, "ACC001", new BigDecimal("50.00"));
        BankAccount dst = account(2L, 2, "ACC002", new BigDecimal("1000.00"));

        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(bankAccountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(dst));
        when(bankAccountRepository.findByCustomerIdForUpdate(1)).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByCustomerIdForUpdate(2)).thenReturn(Optional.of(dst));

        assertThatThrownBy(() ->
                service.transfer(request("ACC001", "ACC002", new BigDecimal("500.00")), null))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void sameSourceAndDestination_throwsException() {
        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.transfer(request("ACC001", "ACC001", new BigDecimal("100.00")), null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("different");
    }

    @Test
    void blockedSourceAccount_throwsException() {
        BankAccount src = account(1L, 1, "ACC001", new BigDecimal("5000.00"));
        src.setStatus(AccountStatus.BLOCKED);
        BankAccount dst = account(2L, 2, "ACC002", new BigDecimal("1000.00"));

        when(idempotencyKeyRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(bankAccountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(dst));
        when(bankAccountRepository.findByCustomerIdForUpdate(1)).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByCustomerIdForUpdate(2)).thenReturn(Optional.of(dst));

        assertThatThrownBy(() ->
                service.transfer(request("ACC001", "ACC002", new BigDecimal("100.00")), null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("BLOCKED");
    }

    @Test
    void idempotencyReplay_doesNotProcessAgain() throws Exception {
        String iKey = "transfer-idem-key";
        String json = "{\"transferId\":\"TRF001\",\"sourceAccountNumber\":\"ACC001\"," +
                "\"destinationAccountNumber\":\"ACC002\",\"amount\":100," +
                "\"status\":\"SUCCESS\"}";
        when(idempotencyKeyRepository.findByIdempotencyKey(iKey))
                .thenReturn(Optional.of(new IdempotencyKey(iKey, "TRF001", 201, json, 1)));

        var response = service.transfer(request("ACC001", "ACC002", new BigDecimal("100.00")), iKey);

        assertThat(response.getTransferId()).isEqualTo("TRF001");
        verify(bankAccountRepository, never()).findByAccountNumber(any());
        verify(transactionsRepository, never()).save(any());
    }
}
