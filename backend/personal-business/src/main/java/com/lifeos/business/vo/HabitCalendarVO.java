package com.lifeos.business.vo;

import java.time.LocalDate;
import java.util.List;

/**
 * 习惯月历数据视图对象
 */
public class HabitCalendarVO {

    private Long habitId;
    private String habitName;
    private int year;
    private int month;
    private int daysInMonth;
    private int monthCheckins;
    private double monthCompletionRate;
    private int currentStreak;
    private int maxStreak;
    private int totalCheckins;
    private List<LocalDate> checkinDates;
    private List<HabitCheckinVO> checkinDetails;

    public HabitCalendarVO() {
    }

    public Long getHabitId() {
        return habitId;
    }

    public void setHabitId(Long habitId) {
        this.habitId = habitId;
    }

    public String getHabitName() {
        return habitName;
    }

    public void setHabitName(String habitName) {
        this.habitName = habitName;
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

    public int getDaysInMonth() {
        return daysInMonth;
    }

    public void setDaysInMonth(int daysInMonth) {
        this.daysInMonth = daysInMonth;
    }

    public int getMonthCheckins() {
        return monthCheckins;
    }

    public void setMonthCheckins(int monthCheckins) {
        this.monthCheckins = monthCheckins;
    }

    public double getMonthCompletionRate() {
        return monthCompletionRate;
    }

    public void setMonthCompletionRate(double monthCompletionRate) {
        this.monthCompletionRate = monthCompletionRate;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public int getTotalCheckins() {
        return totalCheckins;
    }

    public void setTotalCheckins(int totalCheckins) {
        this.totalCheckins = totalCheckins;
    }

    public List<LocalDate> getCheckinDates() {
        return checkinDates;
    }

    public void setCheckinDates(List<LocalDate> checkinDates) {
        this.checkinDates = checkinDates;
    }

    public List<HabitCheckinVO> getCheckinDetails() {
        return checkinDetails;
    }

    public void setCheckinDetails(List<HabitCheckinVO> checkinDetails) {
        this.checkinDetails = checkinDetails;
    }
}

