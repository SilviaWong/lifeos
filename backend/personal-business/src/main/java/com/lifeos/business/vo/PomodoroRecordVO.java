package com.lifeos.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 番茄钟会话详情视图对象
 */
@Schema(description = "番茄钟会话详情")
public class PomodoroRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话 ID", example = "1001")
    private Long id;

    @Schema(description = "所属用户 ID", example = "1")
    private Long userId;

    @Schema(description = "关联目标 ID", example = "101")
    private Long goalId;

    @Schema(description = "关联目标标题", example = "深入精通 Spring Cloud")
    private String goalTitle;

    @Schema(description = "关联待办任务 ID", example = "201")
    private Long todoId;

    @Schema(description = "关联待办标题", example = "完成 Eureka 替换 Nacos 架构方案")
    private String todoTitle;

    @Schema(description = "会话类型：FOCUS(专注), SHORT_BREAK(短休息), LONG_BREAK(长休息)", example = "FOCUS")
    private String sessionType;

    @Schema(description = "会话类型描述", example = "专注")
    private String sessionTypeDesc;

    @Schema(description = "计划专注时长（秒）", example = "1500")
    private Integer plannedSeconds;

    @Schema(description = "实际专注有效时长（秒）", example = "1500")
    private Integer actualSeconds;

    @Schema(description = "会话状态：RUNNING(进行中), PAUSED(已暂停), COMPLETED(已完成), ABORTED(已中止)", example = "COMPLETED")
    private String status;

    @Schema(description = "会话状态描述", example = "已完成")
    private String statusDesc;

    @Schema(description = "开始时间", example = "2026-11-01 14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @Schema(description = "结束时间", example = "2026-11-01 14:25:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endedAt;

    @Schema(description = "结束原因：NORMAL_FINISH, USER_ABORT, TIMEOUT", example = "NORMAL_FINISH")
    private String endReason;

    @Schema(description = "创建时间", example = "2026-11-01 14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public PomodoroRecordVO() {
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

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public String getGoalTitle() {
        return goalTitle;
    }

    public void setGoalTitle(String goalTitle) {
        this.goalTitle = goalTitle;
    }

    public Long getTodoId() {
        return todoId;
    }

    public void setTodoId(Long todoId) {
        this.todoId = todoId;
    }

    public String getTodoTitle() {
        return todoTitle;
    }

    public void setTodoTitle(String todoTitle) {
        this.todoTitle = todoTitle;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getSessionTypeDesc() {
        return sessionTypeDesc;
    }

    public void setSessionTypeDesc(String sessionTypeDesc) {
        this.sessionTypeDesc = sessionTypeDesc;
    }

    public Integer getPlannedSeconds() {
        return plannedSeconds;
    }

    public void setPlannedSeconds(Integer plannedSeconds) {
        this.plannedSeconds = plannedSeconds;
    }

    public Integer getActualSeconds() {
        return actualSeconds;
    }

    public void setActualSeconds(Integer actualSeconds) {
        this.actualSeconds = actualSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

