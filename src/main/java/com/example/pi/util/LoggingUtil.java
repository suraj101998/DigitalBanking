package com.example.pi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging Utility class for consistent and structured logging across the application.
 * Provides convenient methods for different log levels with formatting.
 */
public class LoggingUtil {

    private final Logger logger;

    public LoggingUtil(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    // ==================== INFO LEVEL ====================

    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void infoWithMetadata(String operation, String status, long executionTime) {
        logger.info("Operation: {} | Status: {} | Execution Time: {} ms", operation, status, executionTime);
    }

    // ==================== DEBUG LEVEL ====================

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public void debugWithData(String operation, Object data) {
        logger.debug("Operation: {} | Data: {}", operation, data);
    }

    // ==================== WARN LEVEL ====================

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void warnWithThrowable(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    // ==================== ERROR LEVEL ====================

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void errorWithContext(String operation, String errorMessage, Exception exception) {
        logger.error("Operation: {} | Error: {} | Exception: {}", operation, errorMessage, exception.getMessage(), exception);
    }

    // ==================== BUSINESS OPERATIONS ====================

    public void logTransactionStart(int customerId, String transactionType, long amount) {
        logger.info("🔄 TRANSACTION STARTED | Customer ID: {} | Type: {} | Amount: {}", customerId, transactionType, amount);
    }

    public void logTransactionSuccess(int customerId, String transactionId, long amount, long executionTime) {
        logger.info("✅ TRANSACTION SUCCESS | Customer ID: {} | Transaction ID: {} | Amount: {} | Time: {} ms",
                   customerId, transactionId, amount, executionTime);
    }

    public void logTransactionFailure(int customerId, String reason, long executionTime) {
        logger.error("❌ TRANSACTION FAILED | Customer ID: {} | Reason: {} | Time: {} ms",
                    customerId, reason, executionTime);
    }

    public void logAuthenticationAttempt(String username, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        logger.info("🔐 AUTHENTICATION {} | Username: {}", status, username);
    }

    public void logAuthorizationCheck(String username, String resource, boolean granted) {
        String status = granted ? "GRANTED" : "DENIED";
        logger.info("🔒 AUTHORIZATION {} | User: {} | Resource: {}", status, username, resource);
    }

    public void logDatabaseQuery(String query, long executionTime) {
        logger.debug("🗄️ DATABASE QUERY | Time: {} ms | Query: {}", executionTime, query);
    }

    public void logCacheHit(String cacheKey) {
        logger.debug("💾 CACHE HIT | Key: {}", cacheKey);
    }

    public void logCacheMiss(String cacheKey) {
        logger.debug("💾 CACHE MISS | Key: {}", cacheKey);
    }

    public void logApiCall(String endpoint, String method, int statusCode, long executionTime) {
        logger.info("📡 API CALL | Endpoint: {} | Method: {} | Status: {} | Time: {} ms",
                   endpoint, method, statusCode, executionTime);
    }

    public void logValidationError(String field, String reason) {
        logger.warn("⚠️ VALIDATION ERROR | Field: {} | Reason: {}", field, reason);
    }

    public void logBalanceCheck(int customerId, long balance) {
        logger.debug("💰 BALANCE CHECK | Customer ID: {} | Balance: {}", customerId, balance);
    }

    public void logInsufficientBalance(int customerId, long required, long available) {
        logger.warn("⚠️ INSUFFICIENT BALANCE | Customer ID: {} | Required: {} | Available: {}",
                   customerId, required, available);
    }

    // ==================== PERFORMANCE MONITORING ====================

    public void logSlowQuery(String query, long executionTime, long threshold) {
        if (executionTime > threshold) {
            logger.warn("⏱️ SLOW QUERY DETECTED | Execution Time: {} ms (threshold: {} ms) | Query: {}",
                       executionTime, threshold, query);
        }
    }

    public void logSlowOperation(String operation, long executionTime, long threshold) {
        if (executionTime > threshold) {
            logger.warn("⏱️ SLOW OPERATION DETECTED | Operation: {} | Time: {} ms (threshold: {} ms)",
                       operation, executionTime, threshold);
        }
    }

    // ==================== SECURITY & AUDIT ====================

    public void logSecurityEvent(String event, String details) {
        logger.info("🔐 SECURITY EVENT | Event: {} | Details: {}", event, details);
    }

    public void logAuditTrail(String action, String actor, String resource, String result) {
        logger.info("📋 AUDIT TRAIL | Action: {} | Actor: {} | Resource: {} | Result: {}",
                   action, actor, resource, result);
    }

    public void logUnauthorizedAccess(String username, String resource) {
        logger.warn("🚫 UNAUTHORIZED ACCESS ATTEMPT | User: {} | Resource: {}", username, resource);
    }

    public void logSuspiciousActivity(String activity, String details) {
        logger.warn("🚨 SUSPICIOUS ACTIVITY | Activity: {} | Details: {}", activity, details);
    }

    // ==================== HELPER METHODS ====================

    public Logger getLogger() {
        return logger;
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }
}
