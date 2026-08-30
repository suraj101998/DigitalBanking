package com.example.pi.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Idempotency key store (doc item #7).
 *
 * Clients supply an Idempotency-Key header.  Before processing a transaction
 * we check this table.  If the key already exists we return the stored response
 * without processing a second transaction.
 *
 * Fields:
 *  idempotencyKey  — the client-supplied key (UUID recommended)
 *  transactionId   — the business transaction ID that was created
 *  responseStatus  — HTTP status code of the original response
 *  responseBody    — serialised JSON response body
 *  createdAt       — when the original request was processed
 */
@Entity
@Table(
    name = "idempotency_keys",
    uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key", columnNames = "idempotency_key")
)
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 64, unique = true)
    private String idempotencyKey;

    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @Column(name = "response_status")
    private Integer responseStatus;

    /** Stored as JSON text so we can replay the original response verbatim. */
    @Lob
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "customer_id")
    private Integer customerId;

    public IdempotencyKey() {
    }

    public IdempotencyKey(String idempotencyKey, String transactionId,
                           int responseStatus, String responseBody,
                           Integer customerId) {
        this.idempotencyKey = idempotencyKey;
        this.transactionId  = transactionId;
        this.responseStatus = responseStatus;
        this.responseBody   = responseBody;
        this.customerId     = customerId;
        this.createdAt      = Instant.now();
    }

    public Long getId() { return id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
}
