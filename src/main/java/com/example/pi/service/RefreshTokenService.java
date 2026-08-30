package com.example.pi.service;

import com.example.pi.entity.RefreshToken;
import com.example.pi.entity.User;
import com.example.pi.exception.InvalidTransactionException;
import com.example.pi.repository.RefreshTokenRepository;
import com.example.pi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Issues, validates, and revokes refresh tokens (doc items #67, #68).
 *
 * Token rotation policy:
 *  - Each user has at most one active refresh token (old one is replaced on each refresh).
 *  - Logout deletes the token (immediate revocation — no need to wait for expiry).
 *  - Expiry is configurable via {@code jwt.refresh-expiration} (default: 7 days).
 */
@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository         userRepository;
    private final long                   refreshTokenExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                UserRepository userRepository,
                                @Value("${jwt.refresh-expiration:604800000}") long refreshTokenExpirationMs) {
        this.refreshTokenRepository  = refreshTokenRepository;
        this.userRepository          = userRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Creates a new refresh token for the given user, replacing any existing one (token rotation).
     */
    @Transactional
    public RefreshToken createRefreshToken(Integer userId) {
        // Delete any existing token for this user (one-per-user policy)
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken token = new RefreshToken(
                UUID.randomUUID().toString(),
                userId,
                Instant.now().plusMillis(refreshTokenExpirationMs)
        );

        return refreshTokenRepository.save(token);
    }

    /**
     * Validates the token string and returns the associated {@link RefreshToken}.
     *
     * @throws InvalidTransactionException if the token is unknown or expired.
     */
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTransactionException("Refresh token not found or already revoked"));

        if (token.isExpired()) {
            // Clean up expired token
            refreshTokenRepository.delete(token);
            throw new InvalidTransactionException("Refresh token has expired. Please log in again.");
        }

        return token;
    }

    /**
     * Revokes (deletes) the refresh token for a user — used on logout (doc item #68).
     */
    @Transactional
    public void revokeByUserId(Integer userId) {
        refreshTokenRepository.deleteByUserId(userId);
        logger.info("Refresh token revoked for userId={}", userId);
    }

    /**
     * Loads the {@link User} owning the given refresh token.
     */
    @Transactional(readOnly = true)
    public User getUserByRefreshToken(RefreshToken token) {
        return userRepository.findById(token.getUserId())
                .orElseThrow(() -> new InvalidTransactionException("User for refresh token not found"));
    }
}
