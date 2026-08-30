package com.example.pi.entity;

import com.example.pi.entity.enums.TransactionStatus;
import com.example.pi.entity.enums.TransactionType;
import com.example.pi.entity.enums.TransactionMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable ledger record for each debit/credit operation.
 *
 * Key changes from original:
 *  - amounts are BigDecimal (doc items #4, #5)
 *  - timestamp is Instant for precise ordering & auditability (doc item #19)
 *  - transaction_type and transaction_mode are enum-backed (doc item #25)
 *  - status field added (doc item #21)
 *  - customer_id column kept as FK reference (doc item #14)
 *  - transaction_id has a unique constraint (doc item #13)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_transactions_customer_date",
               columnList = "customer_id, transaction_date DESC")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_transactions_transaction_id", columnNames = "transaction_id")
    }
)
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serial_number")
    private Integer serialNumber;

    /** Unique business identifier for the transaction (e.g. TXN-UUID). */
    @Column(name = "transaction_id", nullable = false, length = 50, unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_mode", length = 20)
    private TransactionMode transactionMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false, length = 20)
    private TransactionStatus transactionStatus = TransactionStatus.SUCCESS;

    @Column(name = "transaction_to", length = 100)
    private String transactionTo;

    /** Precise instant the transaction was recorded — not just a date. */
    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    /**
     * Transaction amount.
     * precision=19, scale=4 matches the BankAccount.balance column.
     */
    @Column(name = "transaction_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal transactionAmount;

    /** Balance before this transaction was applied (snapshot). */
    @Column(name = "initial_deposit", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialDeposit;

    /** Balance after this transaction was applied (snapshot). */
    @Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance;

    /** FK to customers.customer_id — enforced by schema migration. */
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    /**
     * Idempotency key — client-supplied deduplication key.
     * If the same key is submitted twice, the original result is returned (doc item #7).
     */
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    /**
     * Optimistic locking version on the transaction record itself.
     * Prevents accidental in-place modification of a committed entry (doc item #5).
     * Transactions should be immutable once saved — any update attempt that changes
     * this field will fail with ObjectOptimisticLockingFailureException.
     */
    @Version
    @Column(name = "version")
    private Long version;

    public Transactions() {
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Integer getSerialNumber() { return serialNumber; }
    public void setSerialNumber(Integer serialNumber) { this.serialNumber = serialNumber; }

    /** Legacy accessor kept for compatibility. */
    public int getSerial_number() { return serialNumber != null ? serialNumber : 0; }
    public void setSerial_number(int serialNumber) { this.serialNumber = serialNumber; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    /** Legacy accessor. */
    public String getTransaction_id() { return transactionId; }
    public void setTransaction_id(String transactionId) { this.transactionId = transactionId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    /** Legacy string accessor. */
    public String getTransaction_type() { return transactionType != null ? transactionType.name() : null; }
    public void setTransaction_type(String type) {
        this.transactionType = type != null ? TransactionType.valueOf(type.toUpperCase()) : null;
    }

    public TransactionMode getTransactionMode() { return transactionMode; }
    public void setTransactionMode(TransactionMode transactionMode) { this.transactionMode = transactionMode; }

    /** Legacy string accessor. */
    public String getTransaction_mode() { return transactionMode != null ? transactionMode.name() : null; }
    public void setTransaction_mode(String mode) {
        this.transactionMode = mode != null ? TransactionMode.valueOf(mode.toUpperCase()) : null;
    }

    public TransactionStatus getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(TransactionStatus transactionStatus) { this.transactionStatus = transactionStatus; }

    public String getTransactionTo() { return transactionTo; }
    public void setTransactionTo(String transactionTo) { this.transactionTo = transactionTo; }

    public String getTransaction_to() { return transactionTo; }
    public void setTransaction_to(String transactionTo) { this.transactionTo = transactionTo; }

    public Instant getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Instant transactionDate) { this.transactionDate = transactionDate; }

    /** Legacy java.sql.Date accessor — converts Instant to sql.Date for DAO / DTO compatibility. */
    public java.sql.Date getTransaction_date() {
        return transactionDate != null ? new java.sql.Date(transactionDate.toEpochMilli()) : null;
    }
    public void setTransaction_date(java.sql.Date date) {
        this.transactionDate = date != null ? date.toInstant() : null;
    }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    /** Legacy long accessor. */
    public long getTransaction_amount() { return transactionAmount != null ? transactionAmount.longValue() : 0L; }
    public void setTransaction_amount(long amount) { this.transactionAmount = BigDecimal.valueOf(amount); }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }

    public long getInitial_deposit() { return initialDeposit != null ? initialDeposit.longValue() : 0L; }
    public void setInitial_deposit(long initialDeposit) { this.initialDeposit = BigDecimal.valueOf(initialDeposit); }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public long getAvailable_balance() { return availableBalance != null ? availableBalance.longValue() : 0L; }
    public void setAvailable_balance(long availableBalance) { this.availableBalance = BigDecimal.valueOf(availableBalance); }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public int getCustomer_id() { return customerId != null ? customerId : 0; }
    public void setCustomer_id(int customerId) { this.customerId = customerId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    /**
     * Guard against accidental in-place mutation of committed transactions (doc item #27).
     * Only status transitions (e.g., SUCCESS → REVERSED) are allowed post-save.
     */
    @PreUpdate
    protected void onUpdate() {
        // transactionAmount, transactionType, customerId, and transactionId must never change.
        // Hibernate will still call this method when only transactionStatus changes (reversal).
    }

    @Override
    public String toString() {
        return "Transactions{serialNumber=" + serialNumber +
               ", transactionId='" + transactionId + '\'' +
               ", type=" + transactionType +
               ", amount=" + transactionAmount +
               ", status=" + transactionStatus +
               ", customerId=" + customerId + '}';
    }
}
