package com.lifeos.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 开始番茄钟专注会话请求参数
 */
@Schema(description = "开始番茄钟专注请求")
public class StartPomodoroRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话类型：FOCUS(专注), SHORT_BREAK(短休息), LONG_BREAK(长休息)", example = "FOCUS")
    private String sessionType = "FOCUS";

    @Schema(description = "计划专注时长（秒），例如 1500 代表 25 分钟", requiredMode = Schema.RequiredMode.REQUIRED, example = "1500")
    @NotNull(message = "计划时长不能为空")
    @Min(value = 60, message = "计划专注时长不能少于 60 秒 (1 分钟)")
    @Max(value = 14400, message = "单次专注时长不能超过 14400 秒 (4 小时)")
    private Integer plannedSeconds;

    @Schema(description = "关联目标 ID（可选）", example = "101")
    private Long goalId;

    @Schema(description = "关联日程待办 ID（可选）", example = "201")
    private Long todoId;

    public StartPomodoroRequest() {
    }

    public String getSessionType() {
        return sessionType != null ? sessionType : "FOCUS";
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Integer getPlannedSeconds() {
        return plannedSeconds;
    }

    public void setPlannedSeconds(Integer plannedSeconds) {
        this.plannedSeconds = plannedSeconds;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Long getTodoId() {
        return todoId;
    }

    public void setTodoId(Long todoId) {
        this.todoId = todoId;
    }
}

