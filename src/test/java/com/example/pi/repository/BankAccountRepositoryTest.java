package com.example.pi.repository;

import com.example.pi.entity.BankAccount;
import com.example.pi.entity.enums.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest for BankAccountRepository (doc item #57).
 *
 * Uses H2 in-memory — Flyway is disabled in the test profile.
 * Hibernate creates the schema via ddl-auto=create-drop.
 *
 * Covers:
 *  - save and find by account number
 *  - findByCustomerId returns the account
 *  - findByCustomerIdForUpdate (pessimistic-write query) returns the account
 *  - findByAccountNumberAndCustomerId works for composite lookup
 *  - Status defaults to ACTIVE on construction
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private BankAccount persist(String accountNumber, BigDecimal balance, int customerId) {
        BankAccount account = new BankAccount(accountNumber, balance, customerId);
        return bankAccountRepository.save(account);
    }

    @Test
    void save_and_findByAccountNumber() {
        persist("ACC100", new BigDecimal("5000.00"), 1);

        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("ACC100");

        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualByComparingTo("5000.00");
        assertThat(found.get().getCustomerId()).isEqualTo(1);
    }

    @Test
    void defaultStatus_isActive() {
        persist("ACC101", BigDecimal.TEN, 2);

        BankAccount found = bankAccountRepository.findByAccountNumber("ACC101").orElseThrow();
        assertThat(found.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void findByCustomerId_returnsAccount() {
        persist("ACC102", new BigDecimal("1000.00"), 3);

        Optional<BankAccount> found = bankAccountRepository.findByCustomerId(3);

        assertThat(found).isPresent();
        assertThat(found.get().getAccountNumber()).isEqualTo("ACC102");
    }

    @Test
    void findByCustomerIdForUpdate_returnsAccount() {
        persist("ACC103", new BigDecimal("2000.00"), 4);

        Optional<BankAccount> found = bankAccountRepository.findByCustomerIdForUpdate(4);

        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualByComparingTo("2000.00");
    }

    @Test
    void findByAccountNumberAndCustomerId_returnsCorrectAccount() {
        persist("ACC104", new BigDecimal("750.00"), 5);

        Optional<BankAccount> found =
                bankAccountRepository.findByAccountNumberAndCustomerId("ACC104", 5);

        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualByComparingTo("750.00");
    }

    @Test
    void findByAccountNumberAndCustomerId_returnsEmpty_whenMismatch() {
        persist("ACC105", new BigDecimal("300.00"), 6);

        Optional<BankAccount> found =
                bankAccountRepository.findByAccountNumberAndCustomerId("ACC105", 999);

        assertThat(found).isEmpty();
    }

    @Test
    void balanceUpdate_persistsCorrectly() {
        BankAccount account = persist("ACC106", new BigDecimal("1000.00"), 7);
        account.setBalance(new BigDecimal("1500.00"));
        bankAccountRepository.save(account);

        BankAccount updated = bankAccountRepository.findByAccountNumber("ACC106").orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("1500.00");
    }
}
