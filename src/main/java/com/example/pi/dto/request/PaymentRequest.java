package com.example.pi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Payment / transaction request DTO with Bean Validation (doc items #24, #4).
 * transactionAmount is BigDecimal — never use int/long for monetary values.
 */
public class PaymentRequest {

    @NotBlank(message = "Transaction type is required")
    @JsonProperty("transaction_type")
    private String transactionType;

    @NotBlank(message = "Transaction recipient is required")
    @Size(max = 100)
    @JsonProperty("transaction_to")
    private String transactionTo;

    /**
     * Amount must be positive and non-null.
     * BigDecimal is the correct type for monetary values (doc item #4).
     */
    @NotNull(message = "Transaction amount is required")
    @Positive(message = "Transaction amount must be positive")
    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @NotBlank(message = "Transaction mode is required")
    @JsonProperty("transaction_mode")
    private String transactionMode;

    public PaymentRequest() {
    }

    public PaymentRequest(String transactionType, String transactionTo,
                          BigDecimal transactionAmount, String transactionMode) {
        this.transactionType = transactionType;
        this.transactionTo = transactionTo;
        this.transactionAmount = transactionAmount;
        this.transactionMode = transactionMode;
    }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getTransactionTo() { return transactionTo; }
    public void setTransactionTo(String transactionTo) { this.transactionTo = transactionTo; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }
}
