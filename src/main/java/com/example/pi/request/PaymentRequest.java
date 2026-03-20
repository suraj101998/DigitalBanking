package com.example.pi.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequest {
    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("transaction_to")
    private String transactionTo;

    @JsonProperty("transaction_amount")
    private int transactionAmount;

    @JsonProperty("transaction_mode")
    private String transactionMode;

    public PaymentRequest() {
    }

    public PaymentRequest(String transactionType, String transactionTo, int transactionAmount, String transactionMode) {
        this.transactionType = transactionType;
        this.transactionTo = transactionTo;
        this.transactionAmount = transactionAmount;
        this.transactionMode = transactionMode;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionTo() {
        return transactionTo;
    }

    public void setTransactionTo(String transactionTo) {
        this.transactionTo = transactionTo;
    }

    public int getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(int transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionMode() {
        return transactionMode;
    }

    public void setTransactionMode(String transactionMode) {
        this.transactionMode = transactionMode;
    }
}
