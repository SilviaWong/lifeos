package com.lifeos.business.vo;

/**
 * 习惯总览统计视图对象
 */
public class HabitSummaryVO {

    private long totalHabits;
    private long activeHabits;
    private long todayCheckedHabits;
    private double overallCompletionRate;

    public HabitSummaryVO() {
    }

    public HabitSummaryVO(long totalHabits, long activeHabits, long todayCheckedHabits, double overallCompletionRate) {
        this.totalHabits = totalHabits;
        this.activeHabits = activeHabits;
        this.todayCheckedHabits = todayCheckedHabits;
        this.overallCompletionRate = overallCompletionRate;
    }

    public long getTotalHabits() {
        return totalHabits;
    }

    public void setTotalHabits(long totalHabits) {
        this.totalHabits = totalHabits;
    }

    public long getActiveHabits() {
        return activeHabits;
    }

    public void setActiveHabits(long activeHabits) {
        this.activeHabits = activeHabits;
    }

    public long getTodayCheckedHabits() {
        return todayCheckedHabits;
    }

    public void setTodayCheckedHabits(long todayCheckedHabits) {
        this.todayCheckedHabits = todayCheckedHabits;
    }

    public double getOverallCompletionRate() {
        return overallCompletionRate;
    }

    public void setOverallCompletionRate(double overallCompletionRate) {
        this.overallCompletionRate = overallCompletionRate;
    }
}

