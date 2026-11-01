package com.lifeos.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 待办任务统计摘要视图对象
 */
@Schema(description = "待办统计摘要")
public class TodoSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "所有待办总数", example = "24")
    private long totalCount;

    @Schema(description = "今日待办数（截止或计划在今日，或今日已完成）", example = "6")
    private long todayTotalCount;

    @Schema(description = "今日已完成数", example = "4")
    private long todayCompletedCount;

    @Schema(description = "待处理待办总数（TODO + IN_PROGRESS）", example = "8")
    private long pendingCount;

    @Schema(description = "累计已完成待办数", example = "15")
    private long completedCount;

    @Schema(description = "已逾期待办数（未完成且截止时间早于当前系统时间）", example = "1")
    private long overdueCount;

    public TodoSummaryVO() {
    }

    public TodoSummaryVO(long totalCount, long todayTotalCount, long todayCompletedCount, long pendingCount, long completedCount, long overdueCount) {
        this.totalCount = totalCount;
        this.todayTotalCount = todayTotalCount;
        this.todayCompletedCount = todayCompletedCount;
        this.pendingCount = pendingCount;
        this.completedCount = completedCount;
        this.overdueCount = overdueCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getTodayTotalCount() {
        return todayTotalCount;
    }

    public void setTodayTotalCount(long todayTotalCount) {
        this.todayTotalCount = todayTotalCount;
    }

    public long getTodayCompletedCount() {
        return todayCompletedCount;
    }

    public void setTodayCompletedCount(long todayCompletedCount) {
        this.todayCompletedCount = todayCompletedCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }
}

