package com.example.pi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Typed configuration for JWT settings (doc item #61 — @ConfigurationProperties).
 * Replaces ad-hoc @Value fields in JwtUtils.
 *
 * application.properties:
 *   jwt.secret=${JWT_SECRET}
 *   jwt.expiration=3600000
 *   jwt.issuer=digital-banking-api
 *   jwt.audience=digital-banking-clients
 */
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    @NotBlank(message = "JWT secret must not be blank — set the JWT_SECRET environment variable")
    private String secret;

    @Positive(message = "JWT expiration must be positive (milliseconds)")
    private long expiration = 3600000L;

    private String issuer   = "digital-banking-api";
    private String audience = "digital-banking-clients";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
}
