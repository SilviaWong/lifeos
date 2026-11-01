package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 修改目标子任务请求参数
 */
@Schema(description = "修改目标子任务请求参数")
public class UpdateGoalTaskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "子任务标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成案例分析真题 5 套并复盘")
    @NotBlank(message = "子任务标题不能为空")
    @Size(max = 120, message = "子任务标题长度不得超过 120 个字符")
    private String title;

    @Schema(description = "子任务详细说明", example = "重点分析系统可用性与容灾架构设计")
    @Size(max = 500, message = "子任务描述长度不得超过 500 个字符")
    private String description;

    @Schema(description = "排序序号 (升序)", example = "10")
    private Integer sortNo;

    @Schema(description = "截止时间", example = "2026-11-20 20:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueAt;

    public UpdateGoalTaskRequest() {
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

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }
}

