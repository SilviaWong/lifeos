package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财务看板收支汇总统计 VO
 */
public class FinanceStatSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支出总额
     */
    private BigDecimal totalExpense = BigDecimal.ZERO;

    /**
     * 收入总额
     */
    private BigDecimal totalIncome = BigDecimal.ZERO;

    /**
     * 净结余（收入总额 - 支出总额）
     */
    private BigDecimal netBalance = BigDecimal.ZERO;

    /**
     * 记录总笔数
     */
    private Long recordCount = 0L;

    public FinanceStatSummaryVO() {
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }
}

