package com.lifeos.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 番茄钟专属专注统计摘要视图对象
 */
@Schema(description = "番茄钟专注统计摘要")
public class PomodoroSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "今日累计有效专注秒数", example = "5400")
    private Long todayFocusSeconds;

    @Schema(description = "今日累计有效专注分钟数", example = "90")
    private Long todayFocusMinutes;

    @Schema(description = "今日完成番茄钟个数", example = "3")
    private Integer todayCompletedCount;

    @Schema(description = "本周累计有效专注秒数", example = "27000")
    private Long weekFocusSeconds;

    @Schema(description = "本周累计有效专注分钟数", example = "450")
    private Long weekFocusMinutes;

    @Schema(description = "本周完成番茄钟个数", example = "15")
    private Integer weekCompletedCount;

    @Schema(description = "历史累计完成番茄钟个数", example = "42")
    private Long totalCompletedCount;

    @Schema(description = "历史累计有效专注分钟数", example = "1050")
    private Long totalFocusMinutes;

    public PomodoroSummaryVO() {
        this.todayFocusSeconds = 0L;
        this.todayFocusMinutes = 0L;
        this.todayCompletedCount = 0;
        this.weekFocusSeconds = 0L;
        this.weekFocusMinutes = 0L;
        this.weekCompletedCount = 0;
        this.totalCompletedCount = 0L;
        this.totalFocusMinutes = 0L;
    }

    public Long getTodayFocusSeconds() {
        return todayFocusSeconds;
    }

    public void setTodayFocusSeconds(Long todayFocusSeconds) {
        this.todayFocusSeconds = todayFocusSeconds;
    }

    public Long getTodayFocusMinutes() {
        return todayFocusMinutes;
    }

    public void setTodayFocusMinutes(Long todayFocusMinutes) {
        this.todayFocusMinutes = todayFocusMinutes;
    }

    public Integer getTodayCompletedCount() {
        return todayCompletedCount;
    }

    public void setTodayCompletedCount(Integer todayCompletedCount) {
        this.todayCompletedCount = todayCompletedCount;
    }

    public Long getWeekFocusSeconds() {
        return weekFocusSeconds;
    }

    public void setWeekFocusSeconds(Long weekFocusSeconds) {
        this.weekFocusSeconds = weekFocusSeconds;
    }

    public Long getWeekFocusMinutes() {
        return weekFocusMinutes;
    }

    public void setWeekFocusMinutes(Long weekFocusMinutes) {
        this.weekFocusMinutes = weekFocusMinutes;
    }

    public Integer getWeekCompletedCount() {
        return weekCompletedCount;
    }

    public void setWeekCompletedCount(Integer weekCompletedCount) {
        this.weekCompletedCount = weekCompletedCount;
    }

    public Long getTotalCompletedCount() {
        return totalCompletedCount;
    }

    public void setTotalCompletedCount(Long totalCompletedCount) {
        this.totalCompletedCount = totalCompletedCount;
    }

    public Long getTotalFocusMinutes() {
        return totalFocusMinutes;
    }

    public void setTotalFocusMinutes(Long totalFocusMinutes) {
        this.totalFocusMinutes = totalFocusMinutes;
    }
}

