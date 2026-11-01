package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 番茄钟用户配置偏好实体
 */
@TableName("pomodoro_setting")
public class PomodoroSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户 ID
     */
    private Long userId;

    /**
     * 专注时长（分钟，默认 25）
     */
    private Integer focusDuration;

    /**
     * 短休息时长（分钟，默认 5）
     */
    private Integer shortBreakDuration;

    /**
     * 长休息时长（分钟，默认 15）
     */
    private Integer longBreakDuration;

    /**
     * 长休息间隔周期（几个专注后长休息，默认 4）
     */
    private Integer longBreakInterval;

    /**
     * 自动开始休息：0-否, 1-是
     */
    private Integer autoStartBreaks;

    /**
     * 自动开始下一个专注：0-否, 1-是
     */
    private Integer autoStartPomodoros;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    public PomodoroSetting() {
        this.focusDuration = 25;
        this.shortBreakDuration = 5;
        this.longBreakDuration = 15;
        this.longBreakInterval = 4;
        this.autoStartBreaks = 0;
        this.autoStartPomodoros = 0;
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

    public Integer getFocusDuration() {
        return focusDuration;
    }

    public void setFocusDuration(Integer focusDuration) {
        this.focusDuration = focusDuration;
    }

    public Integer getShortBreakDuration() {
        return shortBreakDuration;
    }

    public void setShortBreakDuration(Integer shortBreakDuration) {
        this.shortBreakDuration = shortBreakDuration;
    }

    public Integer getLongBreakDuration() {
        return longBreakDuration;
    }

    public void setLongBreakDuration(Integer longBreakDuration) {
        this.longBreakDuration = longBreakDuration;
    }

    public Integer getLongBreakInterval() {
        return longBreakInterval;
    }

    public void setLongBreakInterval(Integer longBreakInterval) {
        this.longBreakInterval = longBreakInterval;
    }

    public Integer getAutoStartBreaks() {
        return autoStartBreaks;
    }

    public void setAutoStartBreaks(Integer autoStartBreaks) {
        this.autoStartBreaks = autoStartBreaks;
    }

    public Integer getAutoStartPomodoros() {
        return autoStartPomodoros;
    }

    public void setAutoStartPomodoros(Integer autoStartPomodoros) {
        this.autoStartPomodoros = autoStartPomodoros;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

