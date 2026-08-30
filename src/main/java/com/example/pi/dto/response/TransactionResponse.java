package com.example.pi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction response DTO.
 * Uses BigDecimal for monetary fields (doc item #4) and Instant for the timestamp (doc item #19).
 */
public class TransactionResponse {

    private Integer serialNumber;
    private String transactionType;
    private String transactionId;
    private String transactionTo;
    private Instant transactionDate;
    private BigDecimal transactionAmount;
    private Integer customerId;
    private BigDecimal initialDeposit;
    private BigDecimal availableBalance;
    private String transactionMode;
    private String transactionStatus;

    public TransactionResponse() {
    }

    public TransactionResponse(Integer serialNumber, String transactionType, String transactionId,
                               String transactionTo, Instant transactionDate, BigDecimal transactionAmount,
                               Integer customerId, BigDecimal initialDeposit, BigDecimal availableBalance,
                               String transactionMode, String transactionStatus) {
        this.serialNumber = serialNumber;
        this.transactionType = transactionType;
        this.transactionId = transactionId;
        this.transactionTo = transactionTo;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.customerId = customerId;
        this.initialDeposit = initialDeposit;
        this.availableBalance = availableBalance;
        this.transactionMode = transactionMode;
        this.transactionStatus = transactionStatus;
    }

    public Integer getSerialNumber() { return serialNumber; }
    public void setSerialNumber(Integer serialNumber) { this.serialNumber = serialNumber; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getTransactionTo() { return transactionTo; }
    public void setTransactionTo(String transactionTo) { this.transactionTo = transactionTo; }

    public Instant getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Instant transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }

    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }
}
