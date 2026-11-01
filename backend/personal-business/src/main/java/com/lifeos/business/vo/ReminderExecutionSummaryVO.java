package com.lifeos.business.vo;

import java.io.Serializable;

/**
 * 提醒与消息概览统计 VO
 */
public class ReminderExecutionSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则总数
     */
    private int totalRulesCount;

    /**
     * 启用中的规则数
     */
    private int activeRulesCount;

    /**
     * 未读消息数
     */
    private int unreadMessagesCount;

    /**
     * 今日接收到的提醒数
     */
    private int todayMessagesCount;

    public ReminderExecutionSummaryVO() {
    }

    public ReminderExecutionSummaryVO(int totalRulesCount, int activeRulesCount, int unreadMessagesCount, int todayMessagesCount) {
        this.totalRulesCount = totalRulesCount;
        this.activeRulesCount = activeRulesCount;
        this.unreadMessagesCount = unreadMessagesCount;
        this.todayMessagesCount = todayMessagesCount;
    }

    public int getTotalRulesCount() {
        return totalRulesCount;
    }

    public void setTotalRulesCount(int totalRulesCount) {
        this.totalRulesCount = totalRulesCount;
    }

    public int getActiveRulesCount() {
        return activeRulesCount;
    }

    public void setActiveRulesCount(int activeRulesCount) {
        this.activeRulesCount = activeRulesCount;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }

    public int getTodayMessagesCount() {
        return todayMessagesCount;
    }

    public void setTodayMessagesCount(int todayMessagesCount) {
        this.todayMessagesCount = todayMessagesCount;
    }
}

