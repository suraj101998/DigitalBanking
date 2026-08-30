package com.example.pi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Typed configuration for transaction limits (doc item #63).
 * Values are set in application.properties under the prefix "banking.transaction".
 *
 * Example:
 *   banking.transaction.max-single-amount=500000
 *   banking.transaction.daily-limit=1000000
 *   banking.transaction.monthly-limit=5000000
 */
@Component
@ConfigurationProperties(prefix = "banking.transaction")
public class TransactionLimitConfig {

    /** Maximum amount for a single transaction. Default: ₹5,00,000 */
    private BigDecimal maxSingleAmount = new BigDecimal("500000.00");

    /** Maximum total debit amount per customer per calendar day. Default: ₹10,00,000 */
    private BigDecimal dailyLimit = new BigDecimal("1000000.00");

    /** Maximum total debit amount per customer per calendar month. Default: ₹50,00,000 */
    private BigDecimal monthlyLimit = new BigDecimal("5000000.00");

    public BigDecimal getMaxSingleAmount() { return maxSingleAmount; }
    public void setMaxSingleAmount(BigDecimal maxSingleAmount) { this.maxSingleAmount = maxSingleAmount; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
}
