package com.lifeos.business.vo;

import java.math.BigDecimal;

/**
 * 收支趋势数据点视图对象
 */
public class FinanceTrendPointVO {

    /**
     * 月份格式：yyyy-MM（例如 "2026-11"）
     */
    private String month;

    private BigDecimal income;

    private BigDecimal expense;

    private BigDecimal balance;

    public FinanceTrendPointVO() {
        this.income = BigDecimal.ZERO.setScale(2);
        this.expense = BigDecimal.ZERO.setScale(2);
        this.balance = BigDecimal.ZERO.setScale(2);
    }

    public FinanceTrendPointVO(String month, BigDecimal income, BigDecimal expense, BigDecimal balance) {
        this.month = month;
        this.income = income;
        this.expense = expense;
        this.balance = balance;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

