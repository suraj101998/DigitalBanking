package com.example.pi.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet filter that applies a per-IP token-bucket rate limit (doc item #64).
 *
 * Default policy: 100 requests per minute per IP address.
 * Exceeding this results in HTTP 429 Too Many Requests.
 *
 * Design notes:
 *  - Token-bucket algorithm from Bucket4j (fair, burst-friendly).
 *  - Buckets are stored in a ConcurrentHashMap keyed by IP.
 *    For a multi-node deployment this must be replaced with a Redis/Hazelcast-backed
 *    distributed cache (bucket4j-redis). The map-based approach is correct for
 *    single-node deployments and local testing.
 *  - The filter is intentionally NOT a Spring Bean (@Component) to avoid double
 *    registration. It must be registered via FilterRegistrationBean in SecurityConfiguration.
 */
public class RateLimitingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    /** Maximum number of requests allowed per IP within the refill period. */
    private final int capacity;

    /** Duration of one refill period (e.g. 1 minute). */
    private final Duration period;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(int capacity, Duration period) {
        this.capacity = capacity;
        this.period   = period;
    }

    /** Default constructor: 100 requests per minute per IP. */
    public RateLimitingFilter() {
        this(100, Duration.ofMinutes(1));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq = (HttpServletRequest)  req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        String ip      = resolveClientIp(httpReq);
        Bucket bucket  = buckets.computeIfAbsent(ip, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            logger.warn("Rate limit exceeded for IP={}, URI={}", ip, httpReq.getRequestURI());
            httpRes.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpRes.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpRes.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                    "\"message\":\"Rate limit exceeded. Please retry after 1 minute.\"}");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, period)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        // Honour X-Forwarded-For when behind a reverse proxy / load balancer
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
