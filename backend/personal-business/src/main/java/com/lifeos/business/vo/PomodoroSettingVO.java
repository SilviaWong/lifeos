package com.lifeos.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 番茄钟偏好设置展示视图对象
 */
@Schema(description = "番茄钟偏好设置详情")
public class PomodoroSettingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "专注时长（分钟）", example = "25")
    private Integer focusDuration;

    @Schema(description = "短休息时长（分钟）", example = "5")
    private Integer shortBreakDuration;

    @Schema(description = "长休息时长（分钟）", example = "15")
    private Integer longBreakDuration;

    @Schema(description = "长休息间隔（每隔几个专注后长休息）", example = "4")
    private Integer longBreakInterval;

    @Schema(description = "是否自动开启休息计时", example = "false")
    private Boolean autoStartBreaks;

    @Schema(description = "是否自动开启下一轮专注", example = "false")
    private Boolean autoStartPomodoros;

    public PomodoroSettingVO() {
    }

    public PomodoroSettingVO(Integer focusDuration, Integer shortBreakDuration, Integer longBreakDuration,
                             Integer longBreakInterval, Boolean autoStartBreaks, Boolean autoStartPomodoros) {
        this.focusDuration = focusDuration;
        this.shortBreakDuration = shortBreakDuration;
        this.longBreakDuration = longBreakDuration;
        this.longBreakInterval = longBreakInterval;
        this.autoStartBreaks = autoStartBreaks;
        this.autoStartPomodoros = autoStartPomodoros;
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

    public Boolean getAutoStartBreaks() {
        return autoStartBreaks;
    }

    public void setAutoStartBreaks(Boolean autoStartBreaks) {
        this.autoStartBreaks = autoStartBreaks;
    }

    public Boolean getAutoStartPomodoros() {
        return autoStartPomodoros;
    }

    public void setAutoStartPomodoros(Boolean autoStartPomodoros) {
        this.autoStartPomodoros = autoStartPomodoros;
    }
}

