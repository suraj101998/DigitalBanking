package com.example.pi.dto.response;

import java.sql.Date;

public class TransactionResponse {
    private int serialNumber;
    private String transactionType;
    private String transactionId;
    private String transactionTo;
    private Date transactionDate;
    private long transactionAmount;
    private int customerId;
    private long initialDeposit;
    private long availableBalance;
    private String transactionMode;

    public TransactionResponse() {
    }

    public TransactionResponse(int serialNumber, String transactionType, String transactionId, String transactionTo,
                              Date transactionDate, long transactionAmount, int customerId, long initialDeposit,
                              long availableBalance, String transactionMode) {
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
    }

    public int getSerialNumber() { return serialNumber; }
    public void setSerialNumber(int serialNumber) { this.serialNumber = serialNumber; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getTransactionTo() { return transactionTo; }
    public void setTransactionTo(String transactionTo) { this.transactionTo = transactionTo; }

    public Date getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }

    public long getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(long transactionAmount) { this.transactionAmount = transactionAmount; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public long getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(long initialDeposit) { this.initialDeposit = initialDeposit; }

    public long getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(long availableBalance) { this.availableBalance = availableBalance; }

    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }
}
