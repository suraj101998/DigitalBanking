package com.example.pi.dto.response;

public class BalanceResponse {
    private Integer customerId;
    private long availableBalance;
    private long initialDeposit;

    public BalanceResponse() {
    }

    public BalanceResponse(Integer customerId, long availableBalance, long initialDeposit) {
        this.customerId = customerId;
        this.availableBalance = availableBalance;
        this.initialDeposit = initialDeposit;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public long getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(long availableBalance) {
        this.availableBalance = availableBalance;
    }

    public long getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(long initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
}
