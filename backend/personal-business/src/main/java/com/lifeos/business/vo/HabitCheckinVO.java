package com.lifeos.business.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 习惯打卡记录视图对象
 */
public class HabitCheckinVO {

    private Long id;
    private Long habitId;
    private String habitName;
    private LocalDate checkinDate;
    private String note;
    private Integer isMakeup;
    private LocalDateTime createdAt;

    public HabitCheckinVO() {
    }

    public HabitCheckinVO(Long id, Long habitId, String habitName, LocalDate checkinDate, String note, Integer isMakeup, LocalDateTime createdAt) {
        this.id = id;
        this.habitId = habitId;
        this.habitName = habitName;
        this.checkinDate = checkinDate;
        this.note = note;
        this.isMakeup = isMakeup;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(LocalDate checkinDate) {
        this.checkinDate = checkinDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getIsMakeup() {
        return isMakeup;
    }

    public void setIsMakeup(Integer isMakeup) {
        this.isMakeup = isMakeup;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

