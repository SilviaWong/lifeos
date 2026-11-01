package com.lifeos.business.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收支趋势视图对象
 */
public class FinanceTrendVO {

    private List<FinanceTrendPointVO> points;
    private BigDecimal totalPeriodIncome;
    private BigDecimal totalPeriodExpense;
    private BigDecimal totalPeriodBalance;

    public FinanceTrendVO() {
        this.totalPeriodIncome = BigDecimal.ZERO.setScale(2);
        this.totalPeriodExpense = BigDecimal.ZERO.setScale(2);
        this.totalPeriodBalance = BigDecimal.ZERO.setScale(2);
    }

    public FinanceTrendVO(List<FinanceTrendPointVO> points, BigDecimal totalPeriodIncome,
                          BigDecimal totalPeriodExpense, BigDecimal totalPeriodBalance) {
        this.points = points;
        this.totalPeriodIncome = totalPeriodIncome;
        this.totalPeriodExpense = totalPeriodExpense;
        this.totalPeriodBalance = totalPeriodBalance;
    }

    public List<FinanceTrendPointVO> getPoints() {
        return points;
    }

    public void setPoints(List<FinanceTrendPointVO> points) {
        this.points = points;
    }

    public BigDecimal getTotalPeriodIncome() {
        return totalPeriodIncome;
    }

    public void setTotalPeriodIncome(BigDecimal totalPeriodIncome) {
        this.totalPeriodIncome = totalPeriodIncome;
    }

    public BigDecimal getTotalPeriodExpense() {
        return totalPeriodExpense;
    }

    public void setTotalPeriodExpense(BigDecimal totalPeriodExpense) {
        this.totalPeriodExpense = totalPeriodExpense;
    }

    public BigDecimal getTotalPeriodBalance() {
        return totalPeriodBalance;
    }

    public void setTotalPeriodBalance(BigDecimal totalPeriodBalance) {
        this.totalPeriodBalance = totalPeriodBalance;
    }
}

