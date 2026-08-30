package com.example.pi.security;

import com.example.pi.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtUtils (doc item #56).
 *
 * Covers:
 *  - Token generation creates a parseable, valid JWT
 *  - Subject (username) round-trips correctly
 *  - Roles claim round-trips correctly
 *  - customerId claim round-trips correctly
 *  - Token expiry: expired token fails validation
 *  - Tampered token fails validation
 *  - validateToken returns true for a fresh token
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private static final String SECRET =
            "test-secret-key-for-unit-tests-only-not-production-123456789";

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setSecret(SECRET);
        config.setExpiration(3_600_000L);   // 1 hour
        config.setIssuer("test-issuer");
        config.setAudience("test-audience");

        jwtUtils = new JwtUtils(config);
    }

    private UserDetails user(String username) {
        return new User(username, "pw", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    void generateToken_producesNonNullToken() {
        String token = jwtUtils.generateToken(user("alice"), "ROLE_USER", 42);
        assertThat(token).isNotBlank();
    }

    @Test
    void getUserNameFromToken_returnsCorrectSubject() {
        String token = jwtUtils.generateToken(user("alice"), "ROLE_USER", null);
        assertThat(jwtUtils.getUserNameFromToken(token)).isEqualTo("alice");
    }

    @Test
    void getRolesFromToken_returnsRoles() {
        String token = jwtUtils.generateToken(user("bob"), "ROLE_ADMIN,ROLE_USER", null);
        String roles = jwtUtils.getRolesFromToken(token);
        assertThat(roles).contains("ROLE_ADMIN");
        assertThat(roles).contains("ROLE_USER");
    }

    @Test
    void getCustomerIdFromToken_returnsCustomerId() {
        String token = jwtUtils.generateToken(user("carol"), "ROLE_USER", 99);
        assertThat(jwtUtils.getCustomerIdFromToken(token)).isEqualTo(99);
    }

    @Test
    void getCustomerIdFromToken_returnsNullWhenNotPresent() {
        String token = jwtUtils.generateToken(user("dave"), "ROLE_USER", null);
        assertThat(jwtUtils.getCustomerIdFromToken(token)).isNull();
    }

    @Test
    void validateToken_returnsTrueForFreshToken() {
        String token = jwtUtils.generateToken(user("eve"), "ROLE_USER", 1);
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalseForTamperedToken() {
        String token = jwtUtils.generateToken(user("frank"), "ROLE_USER", 1);
        // Tamper the payload section of the JWT
        String[] parts   = token.split("\\.");
        String tampered  = parts[0] + "." + parts[1] + "TAMPERED" + "." + parts[2];
        assertThat(jwtUtils.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        JwtConfig shortConfig = new JwtConfig();
        shortConfig.setSecret(SECRET);
        shortConfig.setExpiration(-1L);   // already expired
        shortConfig.setIssuer("test-issuer");
        shortConfig.setAudience("test-audience");

        JwtUtils shortUtils = new JwtUtils(shortConfig);
        String token = shortUtils.generateToken(user("grace"), "ROLE_USER", 1);
        assertThat(shortUtils.validateToken(token)).isFalse();
    }
}
