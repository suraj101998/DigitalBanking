package com.example.pi.security;

import com.example.pi.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT utility.
 *
 * Improvements (doc items #10, #61):
 *  - Uses JwtConfig (@ConfigurationProperties) instead of raw @Value fields
 *  - Structured roles claim: roles is a List<String> e.g. ["ROLE_ADMIN"]
 *  - Issuer and audience claims added
 *  - Constructor injection
 */
@Component
public class JwtUtils {

    private final JwtConfig jwtConfig;

    public JwtUtils(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    public String generateToken(UserDetails userDetails, String roles, Integer customerId) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roleList = roles != null ? Arrays.asList(roles.split(",")) : List.of();
        claims.put("roles", roleList);
        if (customerId != null) {
            claims.put("customerId", customerId);
        }
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now        = new Date();
        Date expiry     = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtConfig.getIssuer())
                .audience().add(jwtConfig.getAudience()).and()
                .issuedAt(now)
                .expiration(expiry)
                .signWith((javax.crypto.SecretKey) getSigningKey())
                .compact();
    }

    public String getUserNameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public String getRolesFromToken(String token) {
        Object rolesObj = getClaimsFromToken(token).get("roles");
        if (rolesObj instanceof List) {
            return String.join(",", (List<String>) rolesObj);
        }
        return rolesObj != null ? rolesObj.toString() : null;
    }

    public Integer getCustomerIdFromToken(String token) {
        Object customerId = getClaimsFromToken(token).get("customerId");
        return customerId != null ? ((Number) customerId).intValue() : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
