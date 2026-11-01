package com.lifeos.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

/**
 * 修改番茄钟配置偏好请求参数
 */
@Schema(description = "修改番茄钟配置偏好请求")
public class UpdatePomodoroSettingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "专注时长（分钟，1~180）", example = "25")
    @Min(value = 1, message = "专注时长不能少于 1 分钟")
    @Max(value = 180, message = "专注时长不能超过 180 分钟")
    private Integer focusDuration;

    @Schema(description = "短休息时长（分钟，1~60）", example = "5")
    @Min(value = 1, message = "短休息不能少于 1 分钟")
    @Max(value = 60, message = "短休息不能超过 60 分钟")
    private Integer shortBreakDuration;

    @Schema(description = "长休息时长（分钟，1~120）", example = "15")
    @Min(value = 1, message = "长休息不能少于 1 分钟")
    @Max(value = 120, message = "长休息不能超过 120 分钟")
    private Integer longBreakDuration;

    @Schema(description = "长休息间隔周期（几个专注后长休息，1~12）", example = "4")
    @Min(value = 1, message = "长休息间隔至少 1 轮")
    @Max(value = 12, message = "长休息间隔不能超过 12 轮")
    private Integer longBreakInterval;

    @Schema(description = "是否自动开启休息计时", example = "false")
    private Boolean autoStartBreaks;

    @Schema(description = "是否自动开启下一轮专注", example = "false")
    private Boolean autoStartPomodoros;

    public UpdatePomodoroSettingRequest() {
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

