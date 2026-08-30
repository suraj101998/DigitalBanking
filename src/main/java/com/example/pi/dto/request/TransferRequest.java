package com.example.pi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request body for an A→B fund transfer (doc item #9).
 * Both source and destination are identified by their account numbers.
 */
public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    @Size(max = 20)
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    @Size(max = 20)
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Transaction mode is required")
    private String transactionMode;

    @Size(max = 200)
    private String description;

    /**
     * Client-supplied idempotency key (doc item #7).
     * Include the Idempotency-Key HTTP header or this field — both are honoured.
     */
    @Size(max = 64)
    private String idempotencyKey;

    public TransferRequest() {}

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String s) { this.sourceAccountNumber = s; }

    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String d) { this.destinationAccountNumber = d; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
