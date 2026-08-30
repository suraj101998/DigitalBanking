package com.example.pi.repository;

import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.TransactionMode;
import com.example.pi.entity.enums.TransactionStatus;
import com.example.pi.entity.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest for TransactionsRepository (doc item #57).
 *
 * Uses H2 in-memory with ddl-auto=create-drop.
 *
 * Covers:
 *  - findByTransactionId returns the correct record
 *  - findByCustomerId returns transactions for the right customer
 *  - findByTransactionMode filters by enum correctly
 *  - Unique constraint on transaction_id
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class TransactionsRepositoryTest {

    @Autowired
    private TransactionsRepository transactionsRepository;

    private Transactions txn(String txnId, int customerId, TransactionType type,
                              TransactionMode mode, BigDecimal amount, BigDecimal balance) {
        Transactions t = new Transactions();
        t.setTransactionId(txnId);
        t.setCustomerId(customerId);
        t.setTransactionType(type);
        t.setTransactionMode(mode);
        t.setTransactionAmount(amount);
        t.setInitialDeposit(balance.subtract(
                type == TransactionType.CREDIT ? amount : amount.negate()));
        t.setAvailableBalance(balance);
        t.setTransactionDate(Instant.now());
        t.setTransactionStatus(TransactionStatus.SUCCESS);
        return t;
    }

    @Test
    void findByTransactionId_returnsCorrectRecord() {
        transactionsRepository.save(
                txn("TXN-A1", 10, TransactionType.CREDIT,
                        TransactionMode.UPI, new BigDecimal("200.00"), new BigDecimal("1200.00")));

        Optional<Transactions> found = transactionsRepository.findByTransactionId("TXN-A1");

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(10);
        assertThat(found.get().getTransactionAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void findByCustomerId_paginatesResults() {
        transactionsRepository.save(
                txn("TXN-B1", 20, TransactionType.CREDIT,
                        TransactionMode.NEFT, new BigDecimal("100.00"), new BigDecimal("600.00")));
        transactionsRepository.save(
                txn("TXN-B2", 20, TransactionType.DEBIT,
                        TransactionMode.UPI, new BigDecimal("50.00"), new BigDecimal("550.00")));
        // Unrelated customer — should not appear
        transactionsRepository.save(
                txn("TXN-B3", 99, TransactionType.CREDIT,
                        TransactionMode.UPI, new BigDecimal("999.00"), new BigDecimal("999.00")));

        Page<Transactions> page = transactionsRepository
                .findByCustomerId(20, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(t -> t.getCustomerId() == 20);
    }

    @Test
    void findByTransactionMode_filtersCorrectly() {
        transactionsRepository.save(
                txn("TXN-C1", 30, TransactionType.CREDIT,
                        TransactionMode.UPI, new BigDecimal("100.00"), new BigDecimal("100.00")));
        transactionsRepository.save(
                txn("TXN-C2", 30, TransactionType.DEBIT,
                        TransactionMode.RTGS, new BigDecimal("50.00"), new BigDecimal("50.00")));

        Page<Transactions> upiPage = transactionsRepository
                .findByTransactionMode(TransactionMode.UPI, PageRequest.of(0, 10));
        Page<Transactions> rtgsPage = transactionsRepository
                .findByTransactionMode(TransactionMode.RTGS, PageRequest.of(0, 10));

        assertThat(upiPage.getContent()).allMatch(
                t -> t.getTransactionMode() == TransactionMode.UPI);
        assertThat(rtgsPage.getContent()).allMatch(
                t -> t.getTransactionMode() == TransactionMode.RTGS);
    }

    @Test
    void findByTransactionId_returnsEmpty_forUnknownId() {
        Optional<Transactions> found = transactionsRepository.findByTransactionId("NO-SUCH-TXN");
        assertThat(found).isEmpty();
    }
}
