package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建待办任务请求参数
 */
@Schema(description = "创建待办任务请求参数")
public class CreateTodoRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "待办标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成阶段5业务代码编写")
    @NotBlank(message = "待办标题不能为空")
    @Size(max = 120, message = "待办标题长度不得超过 120 个字符")
    private String title;

    @Schema(description = "待办描述", example = "包含CRUD、状态流转和首页统计")
    @Size(max = 2000, message = "待办描述长度不得超过 2000 个字符")
    private String description;

    @Schema(description = "优先级：1-低, 2-中, 3-高, 4-紧急", example = "2")
    @Min(value = 1, message = "优先级最低为 1")
    @Max(value = 4, message = "优先级最高为 4")
    private Integer priority = 2;

    @Schema(description = "关联目标 ID（可选）", example = "1001")
    private Long goalId;

    @Schema(description = "计划开始时间", example = "2026-11-01 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "截止时间", example = "2026-11-01 18:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueAt;

    @Schema(description = "提醒时间", example = "2026-11-01 17:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindAt;

    public CreateTodoRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority != null ? priority : 2;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalDateTime remindAt) {
        this.remindAt = remindAt;
    }
}

