package com.example.pi.entity;

import com.example.pi.entity.enums.AccountStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents a bank account owned by a customer.
 * The balance is the single source of truth — never derive balance from transaction history.
 * @Version provides optimistic locking to protect concurrent balance updates.
 */
@Entity
@Table(
    name = "bank_accounts",
    uniqueConstraints = @UniqueConstraint(name = "uk_bank_accounts_account_number", columnNames = "account_number")
)
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(name = "account_number", nullable = false, length = 20, unique = true)
    private String accountNumber;

    /**
     * The current balance of this account.
     * precision=19, scale=4 handles large amounts with fractional paise/cents.
     * Protected from concurrent mutations by @Version.
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "initial_deposit", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialDeposit;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Optimistic locking version — prevents lost-update race conditions on balance changes.
     * Hibernate automatically increments this on each UPDATE.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /** FK back to the owning customer */
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    public BankAccount() {
    }

    public BankAccount(String accountNumber, BigDecimal initialDeposit, Integer customerId) {
        this.accountNumber = accountNumber;
        this.balance = initialDeposit;
        this.initialDeposit = initialDeposit;
        this.customerId = customerId;
        this.status = AccountStatus.ACTIVE;
        this.currency = "INR";
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    @Override
    public String toString() {
        return "BankAccount{id=" + id +
               ", accountNumber='" + accountNumber + '\'' +
               ", balance=" + balance +
               ", status=" + status +
               ", customerId=" + customerId + '}';
    }
}
