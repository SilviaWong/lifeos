package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 习惯打卡记录实体
 */
@TableName("habit_checkin")
public class HabitCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long habitId;

    private LocalDate checkinDate;

    private String note;

    private Integer isMakeup;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    public HabitCheckin() {
    }

    public HabitCheckin(Long userId, Long habitId, LocalDate checkinDate, String note, Integer isMakeup) {
        this.userId = userId;
        this.habitId = habitId;
        this.checkinDate = checkinDate;
        this.note = note;
        this.isMakeup = isMakeup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHabitId() {
        return habitId;
    }

    public void setHabitId(Long habitId) {
        this.habitId = habitId;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}

