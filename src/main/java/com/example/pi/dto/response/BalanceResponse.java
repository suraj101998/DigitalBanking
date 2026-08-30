package com.example.pi.dto.response;

import java.math.BigDecimal;

/**
 * Balance response DTO.
 * Uses BigDecimal for monetary values (doc item #4).
 */
public class BalanceResponse {

    private Integer customerId;
    private BigDecimal availableBalance;
    private BigDecimal initialDeposit;
    private String currency;
    private String accountStatus;

    public BalanceResponse() {
    }

    public BalanceResponse(Integer customerId, BigDecimal availableBalance, BigDecimal initialDeposit) {
        this.customerId = customerId;
        this.availableBalance = availableBalance;
        this.initialDeposit = initialDeposit;
        this.currency = "INR";
    }

    public BalanceResponse(Integer customerId, BigDecimal availableBalance,
                           BigDecimal initialDeposit, String currency, String accountStatus) {
        this.customerId = customerId;
        this.availableBalance = availableBalance;
        this.initialDeposit = initialDeposit;
        this.currency = currency;
        this.accountStatus = accountStatus;
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}
