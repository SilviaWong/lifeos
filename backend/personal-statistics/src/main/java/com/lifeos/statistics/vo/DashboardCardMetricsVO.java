package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 看板概览卡片指标 VO（明确区分累计数据与时间区间数据）
 */
public class DashboardCardMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 累计总注册用户数 (sys_user.deleted = 0)
     */
    private Long totalUserCount = 0L;

    /**
     * 时间区间内新增注册用户数
     */
    private Long rangeNewUserCount = 0L;

    /**
     * 时间区间内活跃用户数（在区间内产生过操作日志的独立用户数）
     */
    private Long rangeActiveUserCount = 0L;

    /**
     * 今日活跃用户数 (DAU)
     */
    private Long todayActiveUserCount = 0L;

    /**
     * 累计健康记录总数
     */
    private Long totalHealthRecordCount = 0L;

    /**
     * 时间区间内健康记录数
     */
    private Long rangeHealthRecordCount = 0L;

    /**
     * 累计财务支出总额
     */
    private BigDecimal totalExpenseAmount = BigDecimal.ZERO;

    /**
     * 时间区间内财务支出总额
     */
    private BigDecimal rangeExpenseAmount = BigDecimal.ZERO;

    /**
     * 累计财务收入总额
     */
    private BigDecimal totalIncomeAmount = BigDecimal.ZERO;

    /**
     * 时间区间内财务收入总额
     */
    private BigDecimal rangeIncomeAmount = BigDecimal.ZERO;

    /**
     * 时间区间内净结余（区间总收入 - 区间总支出）
     */
    private BigDecimal rangeNetBalance = BigDecimal.ZERO;

    /**
     * 累计专注总分钟数
     */
    private Long totalFocusMinutes = 0L;

    /**
     * 时间区间内专注分钟数
     */
    private Long rangeFocusMinutes = 0L;

    /**
     * 累计待办任务总数
     */
    private Long totalTodoCount = 0L;

    /**
     * 累计已完成待办任务数
     */
    private Long completedTodoCount = 0L;

    /**
     * 累计习惯打卡次数
     */
    private Long totalHabitCheckinCount = 0L;

    public DashboardCardMetricsVO() {
    }

    public Long getTotalUserCount() {
        return totalUserCount;
    }

    public void setTotalUserCount(Long totalUserCount) {
        this.totalUserCount = totalUserCount;
    }

    public Long getRangeNewUserCount() {
        return rangeNewUserCount;
    }

    public void setRangeNewUserCount(Long rangeNewUserCount) {
        this.rangeNewUserCount = rangeNewUserCount;
    }

    public Long getRangeActiveUserCount() {
        return rangeActiveUserCount;
    }

    public void setRangeActiveUserCount(Long rangeActiveUserCount) {
        this.rangeActiveUserCount = rangeActiveUserCount;
    }

    public Long getTodayActiveUserCount() {
        return todayActiveUserCount;
    }

    public void setTodayActiveUserCount(Long todayActiveUserCount) {
        this.todayActiveUserCount = todayActiveUserCount;
    }

    public Long getTotalHealthRecordCount() {
        return totalHealthRecordCount;
    }

    public void setTotalHealthRecordCount(Long totalHealthRecordCount) {
        this.totalHealthRecordCount = totalHealthRecordCount;
    }

    public Long getRangeHealthRecordCount() {
        return rangeHealthRecordCount;
    }

    public void setRangeHealthRecordCount(Long rangeHealthRecordCount) {
        this.rangeHealthRecordCount = rangeHealthRecordCount;
    }

    public BigDecimal getTotalExpenseAmount() {
        return totalExpenseAmount;
    }

    public void setTotalExpenseAmount(BigDecimal totalExpenseAmount) {
        this.totalExpenseAmount = totalExpenseAmount;
    }

    public BigDecimal getRangeExpenseAmount() {
        return rangeExpenseAmount;
    }

    public void setRangeExpenseAmount(BigDecimal rangeExpenseAmount) {
        this.rangeExpenseAmount = rangeExpenseAmount;
    }

    public BigDecimal getTotalIncomeAmount() {
        return totalIncomeAmount;
    }

    public void setTotalIncomeAmount(BigDecimal totalIncomeAmount) {
        this.totalIncomeAmount = totalIncomeAmount;
    }

    public BigDecimal getRangeIncomeAmount() {
        return rangeIncomeAmount;
    }

    public void setRangeIncomeAmount(BigDecimal rangeIncomeAmount) {
        this.rangeIncomeAmount = rangeIncomeAmount;
    }

    public BigDecimal getRangeNetBalance() {
        return rangeNetBalance;
    }

    public void setRangeNetBalance(BigDecimal rangeNetBalance) {
        this.rangeNetBalance = rangeNetBalance;
    }

    public Long getTotalFocusMinutes() {
        return totalFocusMinutes;
    }

    public void setTotalFocusMinutes(Long totalFocusMinutes) {
        this.totalFocusMinutes = totalFocusMinutes;
    }

    public Long getRangeFocusMinutes() {
        return rangeFocusMinutes;
    }

    public void setRangeFocusMinutes(Long rangeFocusMinutes) {
        this.rangeFocusMinutes = rangeFocusMinutes;
    }

    public Long getTotalTodoCount() {
        return totalTodoCount;
    }

    public void setTotalTodoCount(Long totalTodoCount) {
        this.totalTodoCount = totalTodoCount;
    }

    public Long getCompletedTodoCount() {
        return completedTodoCount;
    }

    public void setCompletedTodoCount(Long completedTodoCount) {
        this.completedTodoCount = completedTodoCount;
    }

    public Long getTotalHabitCheckinCount() {
        return totalHabitCheckinCount;
    }

    public void setTotalHabitCheckinCount(Long totalHabitCheckinCount) {
        this.totalHabitCheckinCount = totalHabitCheckinCount;
    }
}

