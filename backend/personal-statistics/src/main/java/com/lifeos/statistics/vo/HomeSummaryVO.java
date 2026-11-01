package com.lifeos.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户端首页综合摘要视图对象
 */
@Schema(description = "用户端首页综合摘要")
public class HomeSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户问候语", example = "下午好，欢迎回到 LifeOS！")
    private String greeting;

    @Schema(description = "当前系统日期", example = "2026-11-01")
    private LocalDate currentDate;

    @Schema(description = "星期几说明", example = "星期日")
    private String dayOfWeekDesc;

    @Schema(description = "今日待办总数", example = "6")
    private long todayTodoCount;

    @Schema(description = "今日已完成待办数", example = "4")
    private long todayCompletedTodoCount;

    @Schema(description = "待处理待办总数（TODO + IN_PROGRESS）", example = "8")
    private long pendingTodoCount;

    @Schema(description = "累计待办总数", example = "24")
    private long totalTodoCount;

    @Schema(description = "已逾期待办数", example = "1")
    private long overdueTodoCount;

    @Schema(description = "今日打卡习惯数（后续阶段接入）", example = "0")
    private long todayHabitCheckinCount;

    @Schema(description = "进行中目标数（后续阶段接入）", example = "0")
    private long activeGoalCount;

    @Schema(description = "今日专注时长（分钟，后续阶段接入）", example = "0")
    private long todayFocusMinutes;

    public HomeSummaryVO() {
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public String getDayOfWeekDesc() {
        return dayOfWeekDesc;
    }

    public void setDayOfWeekDesc(String dayOfWeekDesc) {
        this.dayOfWeekDesc = dayOfWeekDesc;
    }

    public long getTodayTodoCount() {
        return todayTodoCount;
    }

    public void setTodayTodoCount(long todayTodoCount) {
        this.todayTodoCount = todayTodoCount;
    }

    public long getTodayCompletedTodoCount() {
        return todayCompletedTodoCount;
    }

    public void setTodayCompletedTodoCount(long todayCompletedTodoCount) {
        this.todayCompletedTodoCount = todayCompletedTodoCount;
    }

    public long getPendingTodoCount() {
        return pendingTodoCount;
    }

    public void setPendingTodoCount(long pendingTodoCount) {
        this.pendingTodoCount = pendingTodoCount;
    }

    public long getTotalTodoCount() {
        return totalTodoCount;
    }

    public void setTotalTodoCount(long totalTodoCount) {
        this.totalTodoCount = totalTodoCount;
    }

    public long getOverdueTodoCount() {
        return overdueTodoCount;
    }

    public void setOverdueTodoCount(long overdueTodoCount) {
        this.overdueTodoCount = overdueTodoCount;
    }

    public long getTodayHabitCheckinCount() {
        return todayHabitCheckinCount;
    }

    public void setTodayHabitCheckinCount(long todayHabitCheckinCount) {
        this.todayHabitCheckinCount = todayHabitCheckinCount;
    }

    public long getActiveGoalCount() {
        return activeGoalCount;
    }

    public void setActiveGoalCount(long activeGoalCount) {
        this.activeGoalCount = activeGoalCount;
    }

    public long getTodayFocusMinutes() {
        return todayFocusMinutes;
    }

    public void setTodayFocusMinutes(long todayFocusMinutes) {
        this.todayFocusMinutes = todayFocusMinutes;
    }
}

