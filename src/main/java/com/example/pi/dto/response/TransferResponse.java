package com.example.pi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response body for A→B fund transfer (doc item #9).
 */
public class TransferResponse {

    private String transferId;
    private String sourceTransactionId;
    private String destinationTransactionId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String transactionMode;
    private String status;
    private Instant transferredAt;
    private String idempotencyKey;

    public TransferResponse() {}

    public TransferResponse(String transferId, String sourceTransactionId, String destinationTransactionId,
                             String sourceAccountNumber, String destinationAccountNumber,
                             BigDecimal amount, String transactionMode, String status,
                             Instant transferredAt, String idempotencyKey) {
        this.transferId             = transferId;
        this.sourceTransactionId    = sourceTransactionId;
        this.destinationTransactionId = destinationTransactionId;
        this.sourceAccountNumber    = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount                 = amount;
        this.transactionMode        = transactionMode;
        this.status                 = status;
        this.transferredAt          = transferredAt;
        this.idempotencyKey         = idempotencyKey;
    }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public String getSourceTransactionId() { return sourceTransactionId; }
    public void setSourceTransactionId(String s) { this.sourceTransactionId = s; }

    public String getDestinationTransactionId() { return destinationTransactionId; }
    public void setDestinationTransactionId(String d) { this.destinationTransactionId = d; }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String s) { this.sourceAccountNumber = s; }

    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String d) { this.destinationAccountNumber = d; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getTransferredAt() { return transferredAt; }
    public void setTransferredAt(Instant transferredAt) { this.transferredAt = transferredAt; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
