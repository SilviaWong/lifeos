package com.lifeos.business.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收支汇总视图对象
 */
public class FinanceSummaryVO {

    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private int recordCount;
    private List<FinanceCategoryStatVO> expenseCategoryStats;
    private List<FinanceCategoryStatVO> incomeCategoryStats;

    public FinanceSummaryVO() {
        this.totalIncome = BigDecimal.ZERO.setScale(2);
        this.totalExpense = BigDecimal.ZERO.setScale(2);
        this.balance = BigDecimal.ZERO.setScale(2);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public List<FinanceCategoryStatVO> getExpenseCategoryStats() {
        return expenseCategoryStats;
    }

    public void setExpenseCategoryStats(List<FinanceCategoryStatVO> expenseCategoryStats) {
        this.expenseCategoryStats = expenseCategoryStats;
    }

    public List<FinanceCategoryStatVO> getIncomeCategoryStats() {
        return incomeCategoryStats;
    }

    public void setIncomeCategoryStats(List<FinanceCategoryStatVO> incomeCategoryStats) {
        this.incomeCategoryStats = incomeCategoryStats;
    }
}

