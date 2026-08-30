package com.example.pi.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Persistent refresh token for JWT rotation (doc items #67, #68).
 *
 * Each user gets one active refresh token at a time.
 * On refresh, the old token is invalidated and a new one is issued.
 * On logout, the token is deleted (revocation — doc item #68).
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = @Index(name = "idx_refresh_tokens_token", columnList = "token"),
    uniqueConstraints = @UniqueConstraint(name = "uk_refresh_tokens_user_id", columnNames = "user_id")
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The opaque random token value sent to the client. */
    @Column(name = "token", nullable = false, unique = true, length = 128)
    private String token;

    /**
     * FK to the owning user.
     * One-to-one: a user has at most one active refresh token at a time.
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    /** Absolute expiry — the token cannot be used after this instant. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Timestamp of creation — useful for auditing. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public RefreshToken() {
    }

    public RefreshToken(String token, Integer userId, Instant expiresAt) {
        this.token     = token;
        this.userId    = userId;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
}
